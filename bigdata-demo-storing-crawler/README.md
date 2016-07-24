# Ankara Cloud Meetup Big Data Demo - [STORING] Tweet Crawling

1. What is `bigdata-demo-storing-crawler` module?
==============
`bigdata-demo-storing-crawler` receives tweets through Twitter's streaming API 
and pushes them into specified **AWS Firehose** stream to be stored into specified **AWS S3** bucket.

2. Instructions
==============
1. Go to `src/main/resources`
2. Open `aws-credentials.properties` file
  * Enter your AWS access key as property value for `aws.accessKey` property key 
  * Enter your AWS secret key as property value for `aws.secretKey` property key 
3. Open `twitter-credentials.properties` file
  * Enter access token of your Twitter application as property value for `twitter.oauth.accessToken` property key
  * Enter access token secret of your Twitter application as property value for `twitter.oauth.accessTokenSecret` property key
  * Enter consumer key of your Twitter application as property value for `twitter.oauth.consumerKey` property key
  * Enter consumer secret of your Twitter application as property value for `twitter.oauth.consumerSecret` property key
4. Open `twitter-stream.properties` file
  * Enter name of the **AWS Firehose** stream you have created before as property value for `aws.tweet.streamName` property key (i.e. `aws.tweet.streamName=my-tweet-stream`)
5. **AWS Elastic Beanstalk**'s maven [plugin](http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/usage.html) 
needs `~/.aws/credentials` file that contains your AWS access and secret keys. 
So if you don't have, create a credentials file  (`~/.aws/credentials`) and put your AWS access and secret keys into it in the following format:

  ```
  [default]
  aws_access_key_id=XYZ123...
  aws_secret_access_key=ABC567...
  ```

6. Open `pom.xml`
  * Write your AWS account id as value of `aws.user.accountId` under `properties` tag group
  * You can change other properties about application jar name, uploaded jar path, AWS EC2 instance type, etc ...
  * By default, your application jar will be uploaded into `ankaracloudmeetup-bigdata-demo-${aws.user.accountId}` bucket on **AWS S3**
7. Run `build-and-deploy.sh` script
  * Tweet crawler application is built with all its dependencies into a single uber jar
  * Tweet crawler application is deployed into AWS via **AWS Elastic Beanstalk** as single instance application
8. Congratulations!!! You have deployed your tweet crawler application. 
   You can see its status and logs at the [AWS Elastic Beanstalk](console.aws.amazon.com/elasticbeanstalk) console.
   Also you can see crawled tweets at created **AWS S3** bucket foldered by **year/month/day/hour** format.
