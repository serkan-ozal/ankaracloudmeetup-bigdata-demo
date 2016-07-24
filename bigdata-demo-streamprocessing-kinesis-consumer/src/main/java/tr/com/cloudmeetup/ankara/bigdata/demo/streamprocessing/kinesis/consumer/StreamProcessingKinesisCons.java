package tr.com.cloudmeetup.ankara.bigdata.demo.streamprocessing.kinesis.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.model.ResourceNotFoundException;

public class StreamProcessingKinesisCons {

    private static final Logger LOGGER = Logger.getLogger(StreamProcessingKinesisCons.class);
    
    private static final AWSCredentials AWS_CREDENTIALS;
    private static final String APPLICATION_NAME;
    private static final String STREAM_NAME;
    private static final String DYNAMO_DB_RESULT_TABLE_NAME;

    // Initial position in the stream when the application starts up for the first time.
    // Position can be one of LATEST (most recent data) or TRIM_HORIZON (oldest available data)
    private static final InitialPositionInStream APPLICATION_INITIAL_POSITION_IN_STREAM =
            InitialPositionInStream.LATEST;

    static {
        try {
            Properties awsProps = getProperties("aws-credentials.properties");
            AWS_CREDENTIALS = 
                    new BasicAWSCredentials(
                            awsProps.getProperty("aws.accessKey"), 
                            awsProps.getProperty("aws.secretKey"));
            
            Properties streamProps = getProperties("tweet-stream.properties");
            APPLICATION_NAME = streamProps.getProperty("aws.tweet.applicationName");
            STREAM_NAME = streamProps.getProperty("aws.tweet.streamName");
            DYNAMO_DB_RESULT_TABLE_NAME = streamProps.getProperty("aws.tweet.resultTableName");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) throws IOException {
        startTweetConsuming();
    }
    
    private static Properties getProperties(String propFileName) throws IOException {
        Properties props = new Properties();
        try {
            InputStream in = StreamProcessingKinesisCons.class.getClassLoader().getResourceAsStream(propFileName);
            if (in != null) {
                props.load(in);
            } 
            props.putAll(System.getProperties());
            return props;
        } catch (IOException e) {
            LOGGER.error("Error occured while loading properties from " + 
                         "'" + propFileName + "'", e);
            throw e;
        }
    }
    
    private static void startTweetConsuming() throws IOException {
        AWSCredentialsProvider awsCredentialsProvider = new StaticCredentialsProvider(AWS_CREDENTIALS);
        
        String workerId = InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
        KinesisClientLibConfiguration kinesisClientLibConfiguration =
                new KinesisClientLibConfiguration(
                        APPLICATION_NAME,
                        STREAM_NAME,
                        awsCredentialsProvider,
                        workerId);
        kinesisClientLibConfiguration.withInitialPositionInStream(APPLICATION_INITIAL_POSITION_IN_STREAM);

        IRecordProcessorFactory recordProcessorFactory = new TweetRecordProcessorFactory(DYNAMO_DB_RESULT_TABLE_NAME);
        Worker worker = new Worker(recordProcessorFactory, kinesisClientLibConfiguration);

        LOGGER.info(String.format("Running %s to process stream %s as worker %s...\n",
                                  APPLICATION_NAME, STREAM_NAME, workerId));

        int exitCode = 0;
        try {
            worker.run();
        } catch (Throwable t) {
            LOGGER.error("Caught throwable while processing data.", t);
            t.printStackTrace();
            exitCode = 1;
        }
        System.exit(exitCode);
    }   
    
    public static void deleteResources() {
        // Delete the stream
        AmazonKinesis kinesis = new AmazonKinesisClient(AWS_CREDENTIALS);
        LOGGER.info(String.format("Deleting the Amazon Kinesis stream used by the sample. Stream Name = %s.\n", 
                                  STREAM_NAME));
        try {
            kinesis.deleteStream(STREAM_NAME);
        } catch (ResourceNotFoundException ex) {
            // The stream doesn't exist.
        }

        // Delete the table
        AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(AWS_CREDENTIALS);
        LOGGER.info(String.format("Deleting the Amazon DynamoDB table used by the Amazon Kinesis Client Library. " +
                                  "Table Name = %s.\n",
                                  APPLICATION_NAME));
        try {
            dynamoDB.deleteTable(APPLICATION_NAME);
        } catch (com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException ex) {
            // The table doesn't exist.
        }
    }

}
