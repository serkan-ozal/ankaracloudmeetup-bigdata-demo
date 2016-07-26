# Ankara Cloud Meetup Big Data Demo - [BATCH PROCESSING] Querying Processed Tweet Data with Hive on AWS EMR

1. What is `bigdata-demo-batchprocessing-hive` module?
==============
`bigdata-demo-batchprocessing-hive` explains how to query processed tweet data (by `bigdata-demo-batchprocessing-hadoop`).

2. Instructions
==============
1. Open `ankaracloudmeetup-bigdata-demo-hive-query.hql` and replace `${aws.user.accountId}` with your AWS account id.
2. Upload updated `ankaracloudmeetup-bigdata-demo-hive-query.hql` into **AWS S3** wherever you want.
3. Go to [AWS Elastic MapReduce](https://console.aws.amazon.com/elasticmapreduce) console.
4. Click the cluster (`Ankara Cloud Meetup Big Data Demo - Batch Processing Cluster`) you have created before.
5. Click **Add step** to specify Hive query to be executed on the cluster.
6. At the **Add step** screen
  * Select **Hive program** for **Step type** field.
  * Enter name of the current step whatever you want for **Name** field.
  * Select the location of Hive query on the **AWS S3** you have uploaded above for **Script S3 location** field.
7. Enter argument for **Arguments** field to specify analyze date. Arguments format is like this:
```
-d analyzeDate=<year>/<month>/<day>
```
8. Click **Add** and then your specified Hive query will be executed on the cluster.
   You can see its status under **Steps** section. 
  * After it has been added, the status will be `Pending`. 
  * Once it has started, the status will be `Running`.
  * When it has finished, the status will be `Completed`.
9. After it has completed, you can see results dumped to a file under 
   `s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-results/hive/${analyzeDate}/'` directory
   where `${aws.user.accountId}` is your AWS account id 
   and `${analyzeDate}` is specified analyze date in the `year/month/day` format. 
10. Congratulations!!! You have submitted your Hive query to query processed tweet data.
