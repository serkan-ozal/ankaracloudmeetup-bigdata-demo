package tr.com.cloudmeetup.ankara.bigdata.demo.streamprocessing.kinesis.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class DynamoDBHelper {
    
    private static final Logger LOGGER = Logger.getLogger(DynamoDBHelper.class);

    public static final AmazonDynamoDBClient DYNAMO_DB;

    static {
        AmazonDynamoDBClient dynamoDBClient = null;
        try {
            Properties awsProps = getProperties("aws-credentials.properties");
            AWSCredentials awsCredentials = 
                    new BasicAWSCredentials(
                            awsProps.getProperty("aws.accessKey"), 
                            awsProps.getProperty("aws.secretKey"));
            dynamoDBClient = new AmazonDynamoDBClient(awsCredentials);
        } catch (IOException e) {
            LOGGER.error("Unable to initialize DynamoDB client!", e);
        }
        DYNAMO_DB = dynamoDBClient;
    }

    private static Properties getProperties(String propFileName) throws IOException {
        Properties props = new Properties();
        try {
            InputStream in = DynamoDBHelper.class.getClassLoader().getResourceAsStream(propFileName);
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
    
}
