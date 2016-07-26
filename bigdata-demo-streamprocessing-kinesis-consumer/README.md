# Ankara Cloud Meetup Big Data Demo - [STREAM PROCESSING] Consuming and Processing Tweets from Real-Time Stream Through AWS Kinesis

1. What is `bigdata-demo-streamprocessing-kinesis-consumer` module?
==============
`bigdata-demo-streamprocessing-kinesis-consumer` consumes and processes tweets from **AWS Kinesis** stream
and saves realtime results into **AWS DynamoDB**.

2. Instructions
==============
1. Go to `src/main/resources`.
2. Open `aws-credentials.properties` file.
  * Enter your AWS access key as property value for `aws.accessKey` property key. 
  * Enter your AWS secret key as property value for `aws.secretKey` property key.
3. Open `twitter-stream.properties` file.
  * Enter name of the **AWS Kinesis** application (i.e. `ankaracloudmeetup-bigdata-demo-tweet-realtimeapplication`) 
    as property value for `aws.tweet.applicationName` property key.
  * Enter name of the **AWS Kinesis** stream (i.e. `ankaracloudmeetup-bigdata-demo-tweet-realtimestream`) 
    you have created before as property value for `aws.tweet.streamName` property key.
  * Enter name of the **AWS DynamoDB** table (i.e. `ankaracloudmeetup-bigdata-demo-tweet-stream-analyse-results`) 
    you have created above as property value for `aws.tweet.resultTableName` property key.
4. **AWS Elastic Beanstalk**'s maven [plugin](http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/usage.html) 
needs `~/.aws/credentials` file that contains your AWS access and secret keys. 
So if you don't have, create a credentials file  (`~/.aws/credentials`) and put your AWS access and secret keys into it in the following format:

  ```
  [default]
  aws_access_key_id=XYZ123...
  aws_secret_access_key=ABC567...
  ```

5. Open `pom.xml`
  * You can change other properties about application jar name, uploaded jar path, AWS EC2 instance type, etc ...
  * By default, your application jar will be uploaded into `ankaracloudmeetup-bigdata-demo-${aws.user.accountId}` bucket 
    (`${aws.user.accountId}` is your AWS account id) on **AWS S3**.
6. Run `build-and-deploy.sh` script
  * Stream processing consumer application is built  and packaged with all of its dependencies into a single uber jar.
  * Stream processing consumer application is deployed into AWS via **AWS Elastic Beanstalk** 
    as multiple instance (clustered) application with auto-scale group behind load balancer.
7. Congratulations!!! You have deployed your stream processing consumer application. 
   You can see its status and logs at the [AWS Elastic Beanstalk](https://console.aws.amazon.com/elasticbeanstalk) console.
   Also you can monitor minute based realtime results at created **AWS DynamoDB** table.
