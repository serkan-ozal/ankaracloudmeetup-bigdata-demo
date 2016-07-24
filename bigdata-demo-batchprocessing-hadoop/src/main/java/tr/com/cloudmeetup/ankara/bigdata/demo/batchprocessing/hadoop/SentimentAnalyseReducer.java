package tr.com.cloudmeetup.ankara.bigdata.demo.batchprocessing.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class SentimentAnalyseReducer extends Reducer<IntWritable, Text, NullWritable, Text> {

    private static final Logger LOGGER = Logger.getLogger(SentimentAnalyseReducer.class); 
    private static final boolean DYNAMO_DB_ENABLE = DynamoDBHelper.DYNAMO_DB != null;
    
    private String resultTableName;
    private int day;
    private int month;
    private int year;
    
    @Override
    protected void setup(Reducer<IntWritable, Text, NullWritable, Text>.Context context)
            throws IOException, InterruptedException {
        super.setup(context);
        Configuration conf = context.getConfiguration();
        
        resultTableName = conf.get("resultTableName");
        day = Integer.parseInt(conf.get("analyseDay"));
        month = Integer.parseInt(conf.get("analyseMonth"));
        year = Integer.parseInt(conf.get("analyseYear"));
    }
    
    @Override
    protected void reduce(IntWritable key, Iterable<Text> values,
            Reducer<IntWritable, Text, NullWritable, Text>.Context context)
            throws IOException, InterruptedException {
        int hour = key.get();
        long positiveTweets = 0;
        long negativeTweets = 0;
        long neutralTweets = 0;
        int total = 0;
        Iterator<Text> i = values.iterator();
        while (i.hasNext()) {
            Text analyzedTweetDataText = i.next();
            context.write(NullWritable.get(), analyzedTweetDataText);
            total++;
            if (DYNAMO_DB_ENABLE) {
                String analyzedTweetData = analyzedTweetDataText.toString();
                // TSV format delimited by tabs
                String[] analyzedTweetDataFields = analyzedTweetData.split("\t");
                String sentimentAnalyseResultValue = analyzedTweetDataFields[3];
                SentimentAnalyseResult sentimentAnalyseResult = 
                        SentimentAnalyseResult.valueOf(sentimentAnalyseResultValue);
                switch (sentimentAnalyseResult) {
                    case POSITIVE:
                        positiveTweets++;
                        break;
                    case NEGATIVE:
                        negativeTweets++;
                        break;
                    case NEUTRAL:
                        neutralTweets++;
                        break;    
                }
            }    
        }

        LOGGER.info("There are total " + total + " tweets at hour " + hour);

        if (DYNAMO_DB_ENABLE) {
            putResultToDynamoDB(hour, positiveTweets, negativeTweets, neutralTweets);
            
            LOGGER.info("There are total\n" + 
                    "\t - " + positiveTweets + " positive\n" +
                    "\t - " + negativeTweets + " negative\n" +
                    "\t - " + neutralTweets + " neutral\n" +
                    "tweets at hour " + hour);
        }
    }
    
    /*
     * DynamoDB Table:
     * 
     *      Name: ankaracloudmeetup-bigdata-demo-tweet-batch-analyse-results
     *      Fields:
     *          id                  [String] <primary key>
     *          hour                [Number] <secondary index>   
     *          day                 [Number] <secondary index>
     *          month               [Number] <secondary index>
     *          year                [Number] <secondary index>
     *          positiveTweetCount  [Number]
     *          negativeTweetCount  [Number]
     *          neutralTweetCount   [Number]
     */
    
    private void putResultToDynamoDB(int hour, long positiveTweets, long negativeTweets, long neutralTweets) {
        Map<String, AttributeValue> result = new HashMap<String, AttributeValue>();
        result.put("id", new AttributeValue().withS(year + "/" + month + "/" + day + "/" + hour));
        result.put("hour", new AttributeValue().withN(String.valueOf(hour)));
        result.put("day", new AttributeValue().withN(String.valueOf(day)));
        result.put("month", new AttributeValue().withN(String.valueOf(month)));
        result.put("year", new AttributeValue().withN(String.valueOf(year)));
        result.put("positiveTweetCount", new AttributeValue().withN(String.valueOf(positiveTweets)));
        result.put("negativeTweetCount", new AttributeValue().withN(String.valueOf(negativeTweets)));
        result.put("neutralTweetCount", new AttributeValue().withN(String.valueOf(neutralTweets)));
        DynamoDBHelper.DYNAMO_DB.putItem(resultTableName, result);
    }

}
