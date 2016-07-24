package tr.com.cloudmeetup.ankara.bigdata.demo.batchprocessing.hadoop;

import java.io.IOException;
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
            Properties awsProps = PropertiesUtil.getProperties("aws-credentials.properties");
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

}
