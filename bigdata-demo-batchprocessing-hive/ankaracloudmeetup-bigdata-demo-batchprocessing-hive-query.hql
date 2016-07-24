DROP TABLE IF EXISTS ANALYZED_TWITTER_DATA;

CREATE EXTERNAL TABLE ANALYZED_TWITTER_DATA (
  user STRING,
  tweet STRING,
  date STRING,
  sentimentAnalyseResult STRING
)
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY '\t'
LOCATION 's3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-results/hadoop/${analyzeDate}/';

INSERT OVERWRITE DIRECTORY 's3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-results/hive/${analyzeDate}/' 
  SELECT
    user,
    sentimentAnalyseResult,
    COUNT(*) as cnt
  FROM ANALYZED_TWITTER_DATA
  GROUP BY user, sentimentAnalyseResult
  ORDER BY cnt DESC
  LIMIT 10;

DROP TABLE ANALYZED_TWITTER_DATA;
