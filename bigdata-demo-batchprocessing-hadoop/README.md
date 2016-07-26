# Ankara Cloud Meetup Big Data Demo - [BATCH PROCESSING] Processing Tweets from AWS S3 with Hadoop on AWS EMR

1. What is `bigdata-demo-batchprocessing-hadoop` module?
==============
`bigdata-demo-batchprocessing-hadoop` module
	* Loads stored tweet data from **AWS S3**
	* Processes and analyses tweet data on **Hadoop** with **Stanford NLP**
	* Stores analyzed tweet data into **AWS S3**
	* Saves analyze result into **AWS DynamoDB**

2. Instructions
==============
1. Go to `src/main/resources`.
2. Open `aws-credentials.properties` file.
  * Enter your AWS access key as property value for `aws.accessKey` property key.
  * Enter your AWS secret key as property value for `aws.secretKey` property key.
3. **AWS Elastic Beanstalk**'s maven [plugin](http://beanstalker.ingenieux.com.br/beanstalk-maven-plugin/usage.html) 
   needs `~/.aws/credentials` file that contains your AWS access and secret keys. 
   So if you don't have, create a credentials file  (`~/.aws/credentials`) 
   and put your AWS access and secret keys into it in the following format:

  ```
  [default]
  aws_access_key_id=XYZ123...
  aws_secret_access_key=ABC567...
  ```

4. Open `pom.xml`.
  * You can change other properties about job jar name, uploaded jar path, etc ...
  * By default, your job jar will be uploaded into `ankaracloudmeetup-bigdata-demo-${aws.user.accountId}` bucket 
    (`${aws.user.accountId}` is your AWS account id) on **AWS S3**.
5. Run `build-and-upload.sh` script.
  * Batch processing Hadoop jar is built with all its dependencies into a single uber jar.
6. Go to [AWS Elastic MapReduce](https://console.aws.amazon.com/elasticmapreduce) console.
7. Click the cluster (`Ankara Cloud Meetup Big Data Demo - Batch Processing Cluster`) you have created before.
8. Click **Add step** to specify Hadoop jar to be run on the cluster.
9. At the **Add step** screen
  * Select **Custom JAR** for **Step type** field.
  * Enter name of the current step whatever you want for **Name** field.
  * Select the location of Hadoop jar on the **AWS S3** you have uploaded above for **JAR location** field. 
    With default configurations, it will be at 
    `ankaracloudmeetup-bigdata-demo-${aws.user.accountId}/ankaracloudmeetup-bigdata-demo-batchprocessing-hadoop.jar` 
    on **AWS S3** where `${aws.user.accountId}` is your AWS account id.
10. Enter argument for **Arguments** field to specify input path, output path and analyze date.
  * **Input path:** This is the 1st argument. Specifies the **AWS S3** bucket where tweet data is stored.
  * **Output path:** This is the 2nd argument. Specifies the location of process results to be stored on **AWS S3**.
  * **Analyze date:** This is the 3rd (last) argument. Specifies the date of tweet data to be analyzed.
    This argument is optional and date of previous day is used as analyze date if this argument is not specified.
  
  Note that there must be space between all parameters to be parsed. 
  So arguments formats will be like this:
  ```
  s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-store s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-results/hadoop [<year>/<month>/<day>]
  ```
where `${aws.user.accountId}` is your AWS account id 
and `[xyzabc]` means optional parameter (means that don't use `[]` character while specifying parameter).
11. Click **Add** and then your specified Hadoop jar will be executed on the cluster.
    You can see its status under **Steps** section below. 
  * After it has been added, the status will be `Pending`. 
  * Once it has started, the status will be `Running`.
  * When it has finished, the status will be `Completed`.
12. After it has completed
  * You can see analyzed tweet data under the output path
    (i.e. `s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-results/hadoop/${analyzeDate}` 
    where `${aws.user.accountId}` is your AWS account id 
    and `${analyzeDate}` is specified analyze date in the `year/month/day` format), 
    you have specified before, as partitioned files.
  * You can see analyze results in the **AWS DynamoDB** table 
    (i.e. `ankaracloudmeetup-bigdata-demo-tweet-batch-analyse-results`) you have created before.
13. Congratulations!!! You have submitted your Hadoop jar to process stored tweet data as batch 
    and generate analyze results.    
