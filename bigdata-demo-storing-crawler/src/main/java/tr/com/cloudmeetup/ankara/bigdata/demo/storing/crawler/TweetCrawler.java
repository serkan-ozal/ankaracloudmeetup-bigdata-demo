package tr.com.cloudmeetup.ankara.bigdata.demo.storing.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClient;
import com.amazonaws.services.kinesisfirehose.model.DescribeDeliveryStreamRequest;
import com.amazonaws.services.kinesisfirehose.model.DescribeDeliveryStreamResult;
import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

@SuppressWarnings("deprecation")
public class TweetCrawler {

    private static final Logger LOGGER = Logger.getLogger(TweetCrawler.class);
    
    private static final AWSCredentials AWS_CREDENTIALS;

    static {
    	try {
	        Properties awsProps = getProperties("aws-credentials.properties");
	        AWS_CREDENTIALS = 
	        		new BasicAWSCredentials(
	        				awsProps.getProperty("aws.accessKey"), 
	        				awsProps.getProperty("aws.secretKey"));
	        
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public static void main(String[] args) throws TwitterException, IOException {        
    	startTweetCrawling();
    }
    
    private static Properties getProperties(String propFileName) throws IOException {
    	Properties props = new Properties();
        try {
            InputStream in = TweetCrawler.class.getClassLoader().getResourceAsStream(propFileName);
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

    private static void startTweetCrawling() throws IOException {
        Properties streamProps = getProperties("tweet-stream.properties");
        String streamName = streamProps.getProperty("aws.tweet.streamName");
        
    	AmazonKinesisFirehoseClient firehoseClient = new AmazonKinesisFirehoseClient(AWS_CREDENTIALS);
    	DescribeDeliveryStreamRequest describeDeliveryStreamRequest = new DescribeDeliveryStreamRequest();
    	describeDeliveryStreamRequest.setDeliveryStreamName(streamName);
    	
    	while (true) {
    		DescribeDeliveryStreamResult deliveryStreamResult = 
    				firehoseClient.describeDeliveryStream(describeDeliveryStreamRequest);
    		if ("ACTIVE".equals(deliveryStreamResult.getDeliveryStreamDescription().getDeliveryStreamStatus())) {
    			break;
    		}
    		LOGGER.info("Waiting stream " + streamName + " to be activated ...");
    		try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
    	}	
    	
        ////////////////////////////////////////////////////////////////
    	
        Properties twitterProps = getProperties("twitter-credentials.properties");
        
        ConfigurationBuilder twitterConf = new ConfigurationBuilder();
        twitterConf.setIncludeEntitiesEnabled(true);
        twitterConf.setOAuthAccessToken(twitterProps.getProperty("twitter.oauth.accessToken"));
        twitterConf.setOAuthAccessTokenSecret(twitterProps.getProperty("twitter.oauth.accessTokenSecret"));
        twitterConf.setOAuthConsumerKey(twitterProps.getProperty("twitter.oauth.consumerKey"));
        twitterConf.setOAuthConsumerSecret(twitterProps.getProperty("twitter.oauth.consumerSecret"));
        twitterConf.setJSONStoreEnabled(true);

        TwitterStream twitterStream = new TwitterStreamFactory(twitterConf.build()).getInstance();
        twitterStream.addListener(new TweetListener(firehoseClient, streamName));

        Properties filterProps = getProperties("tweet-filters.properties");
        
        String languagesValue = filterProps.getProperty("tweet.filter.languages");
        String[] languages = null;
        if (languagesValue != null) {
            languages = languagesValue.split(",");
        }
        
        String keywordsValue = filterProps.getProperty("tweet.filter.keywords");
        String[] keywords = null;
        if (keywordsValue != null) {
            keywords = keywordsValue.split(",");
        }
        
        String locationsValue = filterProps.getProperty("tweet.filter.locations");
        double[][] locations = null;
        if (locationsValue != null) {
            String[] locationsValueParts = locationsValue.split(",");
            locations = new double[locationsValueParts.length / 2][2];
            for (int i = 0; i < locations.length; i++) {
                locations[i] = new double[2];
                locations[i][0] = Double.parseDouble(locationsValueParts[i * 2]);
                locations[i][1] = Double.parseDouble(locationsValueParts[i * 2 + 1]);
            }
        } else {
            locations = new double[][] { { -180, -90 }, { 180, 90 } };
        }
        
        FilterQuery tweetFilterQuery = new FilterQuery();
        if (keywords != null && keywords.length > 0) {
            tweetFilterQuery.track(keywords);
        }
        if (languages != null && languages.length > 0) {
            tweetFilterQuery.language(languages);
        }
        tweetFilterQuery.locations(locations);
        twitterStream.filter(tweetFilterQuery);
    }
    
    private static class TweetListener implements StatusListener {

    	private final AmazonKinesisFirehoseClient firehoseClient;
    	private final String streamName;
    	
    	private TweetListener(AmazonKinesisFirehoseClient firehoseClient, String streamName) {
    		this.firehoseClient = firehoseClient;
    		this.streamName = streamName;
		}
    	
		@Override
        public void onStatus(Status status) {
		    if (LOGGER.isDebugEnabled()) {
		        LOGGER.debug("Tweet by @" + status.getUser().getScreenName() + ": " + status.getText());
		    }
            String tweetJsonData = DataObjectFactory.getRawJSON(status) + "\n";
            ByteBuffer tweetData = ByteBuffer.wrap(tweetJsonData.getBytes());
            PutRecordRequest putRecordRequest = 
                    new PutRecordRequest()
                        .withDeliveryStreamName(streamName)
                        .withRecord(new Record().withData(tweetData));
            firehoseClient.putRecord(putRecordRequest);
        }
        
        @Override
        public void onException(Exception ex) {
            LOGGER.error("On Exception!", ex);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
        }

        @Override
        public void onStallWarning(StallWarning warning) {
        }
        
    }
    
}
