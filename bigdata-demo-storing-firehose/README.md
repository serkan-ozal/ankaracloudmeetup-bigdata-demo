# Ankara Cloud Meetup Big Data Demo - [STORING] Delivery Stream Definition on AWS Firehose

1. What is `bigdata-demo-storing-firehose` module?
==============
`bigdata-demo-storing-firehose` explains how to define **AWS Firehose** stream where listened tweets will be pushed into to be stored. 

2. Instructions
==============
1. Go to [AWS S3](console.aws.amazon.com/s3) console and open **Kinesis Firehose** page
2. Click **Create Bucket**, enter the name of bucket where you want to store tweets and click **Create**
3. Go to [AWS Firehose](console.aws.amazon.com/firehose) console
4. Click **Create Delivery Stream**
5. Select **Destination** for **Amazon S3** field
6. Enter name of the stream, where tweets will be pushed, for **Delivery stream name** field
7. Select **S3 bucket** as the **AWS S3** bucket name you created before to store tweets 
8. Click **Next**
9. Configure buffering, compression, encryption and logging options if you want
10. Click **IAM role** under **IAM Role** section and select **Firehose delivery IAM role**
11. At the redirected page, enter a specific role name or use default role name and click **Allow**
12. Then you will be redirected back to previous page. Then click **Next** here
13. Review **AWS Firehose** stream configurations and click **Create Delivery Stream** if they are OK
14. Then you will be redirected to **AWS Firehose** console home page and you will see your stream in the list.
15. Your stream's state will be **CREATING** during sometime (~15-20 seconds) and its state will be updated as **ACTIVE** when it is ready to use
16. Congratulations!!! You have created your **AWS Firehose** stream for pushing tweets to be stored
