# Ankara Cloud Meetup Big Data Demo - [BATCH PROCESSING] Scheduling Batch Processing with AWS Data Pipeline

1. What is `bigdata-demo-batchprocessing-datapipeline` module?
==============
`bigdata-demo-batchprocessing-datapipeline` explains how to define **AWS Pipeline** flow 
to schedule batch processing activities (Hadoop + Hive) daily.

2. Instructions
==============
1. Open `ankaracloudmeetup-bigdata-demo-batchprocessing-datapipeline.json` 
   and replace `${aws.user.accountId}` with your AWS account id.
2. Go to [AWS Data Pipeline](https://console.aws.amazon.com/datapipeline) console.
3. Click **Create new pipeline** and then you will be redirected to pipeline creation page.
4. Enter name of the pipeline (i.e. `Ankara Cloud Meetup Big Data Demo Batch Processing Pipeline`) 
   whatever you want for **Name** field.
5. Select **Import a definition** for **Source** field.
6. Click **Load local file**, select `ankaracloudmeetup-bigdata-demo-batchprocessing-datapipeline.json` file on your local.
7. Specify logging path or disable it by selecting **Disabled** checkbox under the **Pipeline Configuration** section. 
8. Click **Edit in Architect** and then you will be forwarded to pipeline editor screen.
8. Review your pipeline, update its configuration if you want and click **Save** at above if it is OK.
9. You will see a dialog says that flow has validation warnings. Skip this message and click **Activate**.
10. At first, pipeline runs once it is activated due to scheduler configuration in our pipeline definition. 
    Then, it runs at the same time each day. 
    So every day, batch processing activities (Hadoop + Hive) will be executed automatically.
11. Congratulations!!! You have created your **AWS Data Pipeline** flow 
    to schedule batch processing activities (Hadoop + Hive) daily.
