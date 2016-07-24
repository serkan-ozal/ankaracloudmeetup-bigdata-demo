package tr.com.cloudmeetup.ankara.bigdata.demo.streamprocessing.kinesis.consumer;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;

public class TweeRecordProcessor implements IRecordProcessor {

    private static final Logger LOGGER = Logger.getLogger(TweeRecordProcessor.class);
    
    // Backoff and retry settings
    private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
    private static final int NUM_RETRIES = 10;

    // Checkpoint about once a minute
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L;
    
    private static final CharsetDecoder DECODER = Charset.forName("UTF-8").newDecoder();
    
    private static final DateFormat GROUPED_RESULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    private String kinesisShardId;
    private long nextCheckpointTimeInMillis;

    private final Map<String, CumulativeTweetSentimentAnalysisResult> groupedResultsByMinute = 
            new HashMap<String, CumulativeTweetSentimentAnalysisResult>();
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private final String resultTableName;

    static {
        // Ensure the JVM will refresh the cached IP values of AWS resources (e.g. service endpoints).
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
    }

    public TweeRecordProcessor(String resultTableName) {
        this.resultTableName = resultTableName;
    }
    
    @Override
    public void initialize(String shardId) {
        LOGGER.info("Initializing record processor for shard: " + shardId);
        
        kinesisShardId = shardId;
        scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    processGroupedResults();
                } catch (Throwable t) {
                    LOGGER.error("Error occured while processing grouped results!", t);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
        LOGGER.info("Processing " + records.size() + " records from " + kinesisShardId);

        // Process records and perform all exception handling.
        processRecordsWithRetries(records);

        // Checkpoint once every checkpoint interval.
        if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
            checkpoint(checkpointer);
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        }
    }

    /**
     * Process records performing retries as needed. Skip "poison pill" records.
     * 
     * @param records Data records to be processed.
     */
    private void processRecordsWithRetries(List<Record> records) {
        for (Record record : records) {
            boolean processedSuccessfully = false;
            for (int i = 0; i < NUM_RETRIES; i++) {
                try {
                    processSingleRecord(record);

                    processedSuccessfully = true;
                    break;
                } catch (Throwable t) {
                    LOGGER.warn("Caught throwable while processing record " + record, t);
                }

                // backoff if we encounter an exception.
                try {
                    Thread.sleep(BACKOFF_TIME_IN_MILLIS);
                } catch (InterruptedException e) {
                    LOGGER.debug("Interrupted sleep", e);
                }
            }

            if (!processedSuccessfully) {
                LOGGER.error("Couldn't process record " + record + ". Skipping the record.");
            }
        }
    }

    /**
     * Process a single record.
     * 
     * @param record The record to be processed.
     */
    @SuppressWarnings("deprecation")
    private void processSingleRecord(Record record) {
        String tweetJsonData = null;
        try {
            // For this app, we interpret the payload as UTF-8 chars.
            tweetJsonData = DECODER.decode(record.getData()).toString();
            
            JSONObject tweetJsonObj = new JSONObject(tweetJsonData);
            String tweet = tweetJsonObj.getString("text");
            String user = tweetJsonObj.getJSONObject("user").getString("screen_name");
            Date date = new Date(tweetJsonObj.getString("created_at"));
            SentimentAnalyseResult sentimentAnalyseResult = SentimentAnalyseHelper.doSentimentAnalyze(tweet);
            
            processIntoGroupedResults(date, sentimentAnalyseResult);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Processed data:");
                LOGGER.debug("\t - Tweet Text               : " + tweet);
                LOGGER.debug("\t - Sent By                  : " + user);
                LOGGER.debug("\t - Sent at                  : " + date);
                LOGGER.debug("\t - Sentiment Analyse Result : " + sentimentAnalyseResult);
            }    
        } catch (CharacterCodingException e) {
            LOGGER.error("Malformed data!", e);
        }
    }

    @Override
    public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
        LOGGER.info("Shutting down record processor for shard: " + kinesisShardId);
        
        // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
        if (reason == ShutdownReason.TERMINATE) {
            checkpoint(checkpointer);
        }
        
        scheduledExecutor.shutdown();
        try {
            scheduledExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
        }
        
        try {
            processGroupedResults();
        } catch (Throwable t) {
            LOGGER.error("Error occured while processing grouped results!", t);
        }
        groupedResultsByMinute.clear();
    }

    private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        LOGGER.info("Checkpointing shard " + kinesisShardId);
        
        for (int i = 0; i < NUM_RETRIES; i++) {
            try {
                checkpointer.checkpoint();
                break;
            } catch (ShutdownException se) {
                // Ignore checkpoint if the processor instance has been shutdown (fail over).
                LOGGER.warn("Caught shutdown exception, skipping checkpoint.", se);
                break;
            } catch (ThrottlingException e) {
                // Backoff and re-attempt checkpoint upon transient failures
                if (i >= (NUM_RETRIES - 1)) {
                    LOGGER.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                    break;
                } else {
                    LOGGER.warn("Transient issue when checkpointing - attempt " + 
                                (i + 1) + " of " + NUM_RETRIES, e);
                }
            } catch (InvalidStateException e) {
                // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
                LOGGER.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
                break;
            }
            try {
                Thread.sleep(BACKOFF_TIME_IN_MILLIS);
            } catch (InterruptedException e) {
                LOGGER.debug("Interrupted sleep", e);
            }
        }
    }

    private void processIntoGroupedResults(Date date, SentimentAnalyseResult sentimentAnalyseResult) {
        synchronized (groupedResultsByMinute) {
            String groupedResultKey = GROUPED_RESULT_DATE_FORMAT.format(date);
            CumulativeTweetSentimentAnalysisResult groupedResult = 
                    groupedResultsByMinute.get(groupedResultKey);
            if (groupedResult == null) {
                groupedResult = new CumulativeTweetSentimentAnalysisResult();
                groupedResultsByMinute.put(groupedResultKey, groupedResult);
            }
            if (sentimentAnalyseResult == SentimentAnalyseResult.POSITIVE) {
                groupedResult.positiveTweetCount++;
            } else if (sentimentAnalyseResult == SentimentAnalyseResult.NEGATIVE) {
                groupedResult.negativeTweetCount++;
            } else {
                groupedResult.neutralTweetCount++;
            }
        }
    }
    
    private void processGroupedResults() {
        Date currentTime = Calendar.getInstance().getTime();
        synchronized (groupedResultsByMinute) {
            Iterator<Map.Entry<String, CumulativeTweetSentimentAnalysisResult>> iter = 
                    groupedResultsByMinute.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, CumulativeTweetSentimentAnalysisResult> entry = iter.next();
                String groupedResultKey = entry.getKey();
                CumulativeTweetSentimentAnalysisResult groupedResult = entry.getValue();
                if (groupedResult.isEmpty()) {
                    iter.remove();
                } else {
                    try {
                        Date groupedResultTime = GROUPED_RESULT_DATE_FORMAT.parse(groupedResultKey);
                        long millisBetween = currentTime.getTime() - groupedResultTime.getTime();
                        if (millisBetween < 0) {
                            LOGGER.error("Grouped result time " + "(" + groupedResultKey + ")" + 
                                         " must not before from the current time " + "(" + groupedResultKey + ")");
                            LOGGER.error("Therefore, removing invalid grouped result entry ...");
                            iter.remove();
                        } else {
                            int minutesBetween = (int) (millisBetween / 1000 / 60);
                            if (minutesBetween > 0) {
                                LOGGER.info("Saving grouped result for time " + groupedResultKey + " ...");
                                saveGroupedResultToDynamoDB(groupedResultTime, groupedResult);
                                iter.remove();
                            }
                        }    
                    } catch (ParseException e) {
                        LOGGER.error("Invalid grouped result time: " + groupedResultKey, e);
                        LOGGER.error("Therefore, removing invalid grouped result entry ...");
                        iter.remove();
                    }
                }    
            }
        }
    }
    
    /*
     * DynamoDB Table:
     * 
     *      Name: ankaracloudmeetup-bigdata-demo-tweet-stream-analyse-results
     *      Fields:
     *          id                  [String] <primary key>
     *          groupedYear         [Number]    
     *          groupedMonth        [Number]
     *          groupedDay          [Number]
     *          groupedHour         [Number]
     *          groupedMinute       [Number]
     *          positiveTweetCount  [Number]
     *          negativeTweetCount  [Number]
     *          neutralTweetCount   [Number]
     */
    
    @SuppressWarnings("deprecation")
    private void saveGroupedResultToDynamoDB(Date groupedResultTime, CumulativeTweetSentimentAnalysisResult groupedResult) {
        int year = groupedResultTime.getYear() + 1900;
        int month = groupedResultTime.getMonth() + 1;
        int day = groupedResultTime.getDate();
        int hour = groupedResultTime.getHours();
        int minute = groupedResultTime.getMinutes();
        String groupId = year + "/" + month + "/" + day + " " + hour + ":" + minute;
        
        Map<String, AttributeValue> result = new HashMap<String, AttributeValue>();
        result.put("id", new AttributeValue().withS(kinesisShardId + "[" + groupId + "]"));
        result.put("groupedYear", new AttributeValue().withN(String.valueOf(year)));
        result.put("groupedMonth", new AttributeValue().withN(String.valueOf(month)));
        result.put("groupedDay", new AttributeValue().withN(String.valueOf(day)));
        result.put("groupedHour", new AttributeValue().withN(String.valueOf(hour)));
        result.put("groupedMinute", new AttributeValue().withN(String.valueOf(minute)));
        result.put("positiveTweetCount", new AttributeValue().withN(String.valueOf(groupedResult.positiveTweetCount)));
        result.put("negativeTweetCount", new AttributeValue().withN(String.valueOf(groupedResult.negativeTweetCount)));
        result.put("neutralTweetCount", new AttributeValue().withN(String.valueOf(groupedResult.neutralTweetCount)));
        DynamoDBHelper.DYNAMO_DB.putItem(resultTableName, result);
    }

    private static class CumulativeTweetSentimentAnalysisResult {
        
        private int positiveTweetCount;
        private int negativeTweetCount;
        private int neutralTweetCount;
        
        private boolean isEmpty() {
            return  positiveTweetCount == 0 && 
                    negativeTweetCount == 0 && 
                    neutralTweetCount == 0;
        }
        
    }

}

