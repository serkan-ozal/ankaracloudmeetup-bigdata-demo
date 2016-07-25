# Ankara Cloud Meetup Big Data Demo - [STORING] Delivery Stream Definition on AWS Firehose

1. What is `bigdata-demo-storing-firehose` module?
==============
`bigdata-demo-storing-firehose` explains how to define **AWS Firehose** stream 
that listened tweets will be pushed into to be stored. 

2. Instructions
==============
1. Go to [AWS S3](https://console.aws.amazon.com/s3) console and open **Kinesis Firehose** page.
2. Click **Create Bucket**, enter the name of bucket 
   (i.e. `ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-store` 
   where `${aws.user.accountId}` is your AWS account id) where you want to store tweets and click **Create**.
3. Go to [AWS Firehose](https://console.aws.amazon.com/firehose) console.
4. Click **Create Delivery Stream**.
5. Select **Destination** for **Amazon S3** field.
6. Enter name of the stream (i.e. `ankaracloudmeetup-bigdata-demo-tweet-stream`) for **Delivery stream name** field.
7. Select **S3 bucket** for **AWS S3** field as bucket name 
   (i.e. `ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-store` 
   where `${aws.user.accountId}` is your AWS account id) you have created before to store tweets. 
8. Click **Next**.
9. Configure buffering, compression, encryption and logging options if you want.
10. Click **IAM role** field under **IAM Role** section and select **Firehose delivery IAM role**.
11. Then, you will be redirected to a page to create a role for the **AWS Firehose** stream 
    to access associated **AWS S3** bucket.
12. At the redirected page, you can create a new role, use an existing role or use default role for this case.
    In here, we are using default role create for us and click **Allow**.
13. Then you will be redirected back to previous page which is the configuration page for the stream. 
14. Click **Next**.
15. Review **AWS Firehose** stream configurations and click **Create Delivery Stream** if they are OK.
16. Then you will be redirected to **AWS Firehose** console home page and you will see your stream in the list.
17. Your stream's state will be `CREATING` during sometime (~15-20 seconds) 
    and its state will be updated as `ACTIVE` when it is ready to use.
18. Congratulations!!! You have created your **AWS Firehose** stream for pushing tweets into to be stored.
