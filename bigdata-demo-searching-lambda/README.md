# Ankara Cloud Meetup Big Data Demo - [SEARCHING] Saving Tweets Through AWS Lambda into AWS Elasticsearch to be able to Searched

1. What is `bigdata-demo-searching-lambda` module?
==============
`bigdata-demo-searching-lambda` explains how to define **AWS Lambda** function to be notified 
while saving listened tweets into **AWS S3** and putting them into **AWS Elasticsearch** to be indexed.

2. Instructions
==============
1. Go to [AWS Lambda](https://console.aws.amazon.com/lambda) console.
2. Skip **Selecting blueprint** screen by clicking **Next**. 
   **Blueprint**s are sample configurations of event sources and Lambda functions.
   Then you will be forwarded to **Configure triggers** screen.
3. Select **S3** as trigger.
4. Select name of the bucket (i.e. `ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-store` 
   where `${aws.user.accountId}` is your AWS account id), 
   that you have created before for storing pushed tweets, for **Bucket** field.
5. Select **Object Created (All)** for **Event type** field.
6. Select (tick) **Enable trigger** field.
7. Then click **Next** and you will be forwarded to **Configure function** screen.
8. Enter name of the function what ever you want (i.e. `ankaracloudmeetup-bigdata-demo-searching-lambda-s3-to-es-python`).
9. Enter description as `{"aws.search.domainEndpoint":"<aws_elasticsearch_endpoint>"}` 
   where `<aws_elasticsearch_endpoint>` is the URL of **AWS Elasticsearch** domain you have created and noted before.
10. Select **Python** for **Runtime** field.
11. Select **Upload a .ZIP file** for **Code entry type** field.
12. Click **Upload** button and select `ankaracloudmeetup-bigdata-demo-searching-lambda-s3-to-es-python.zip` file.
13. Select **Create a custom role** for **Role** field 
    and then you will be redirected to IAM role page for creating a new custom role.
14. Enter name of the role what ever you want (i.e. `ankaracloudmeetup-bigdata-demo-searching-lambda-s3-to-es-exec-role-policy`).
15. Expand **View Policy Document**, click **Edit** and click **OK** for the confirmation about edit.
16. Copy the content of `ankaracloudmeetup-bigdata-demo-searching-lambda-exec-role-policy` as content of policy.
17. Then click **Allow**, so you will be redirected back to **Configure function** screen.
18. Select `512` (should be `256` minimum) for **Memory (MB)** field 
    and `1 min 0 sec` (or whatever you want) for **Timeout** field.
19. Then click **Next**, so you will be forwarded to **Review** screen.
20. Review **AWS Lambda** function configurations and click **Create function** if they are OK. 
21. After **AWS Lambda** function has been created, it will be triggered 
    whenever a new file (contains tweet data) is uploaded to associated **AWS S3** bucket by created **AWS Firehose** stream.
22. You can query, analyze and visualize your indexed tweet data through **Kibana** 
    from  `<aws_elasticsearch_endpoint>/_plugin/kibana/` URL where
    `<aws_elasticsearch_endpoint>` is the URL of **AWS Elasticsearch** domain you have created and noted before.
23. On **Kibana** dashboard, type `twitter` for **Index name or pattern** field 
    and type `timestamp_ms` for **Time-field name** field. Then click **Create**. 
    This will create index pattern on **Kibana** to analyze indexed tweet data on **Elasticsearch** domain.
24. Finally, you can make your query&analyze on **Discover** panel and visualize the results.     
25. Congratulations!!! You have created your **AWS Lambda** function to be notified while saving listened tweets into **AWS S3** 
    and putting them into **AWS Elasticsearch** to be indexed.
