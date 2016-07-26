# Ankara Cloud Meetup Big Data Demo - [STREAM PROCESSING] Realtime Stream Definition on AWS Kinesis

1. What is `bigdata-demo-streamprocessing-kinesis` module?
==============
`bigdata-demo-streamprocessing-kinesis` explains how to define **AWS Kinesis** stream 
where listened tweets will be pushed into to be analyzed. 

2. Instructions
==============
1. Go to [AWS Kinesis](https://console.aws.amazon.com/kinesis) console and open **Kinesis Streams** page.
2. Click **Create Stream** and then you will be forwarded to stream creation page.
3. Enter name of the stream (i.e. `ankaracloudmeetup-bigdata-demo-tweet-realtimestream`), 
   which you want to push tweets, for **Stream Name** field.
4. Enter shard count of the stream for **Number of Shards** field by considering your data throughput.
5. And then click **Create** to start process of stream creation.
6. Your stream's state will be `CREATING` during sometime (~1 minute) 
   and its state will be updated as `ACTIVE` when it is ready to use.
   So wait until stream's state becomes `ACTIVE`.
7. So you will be able to push tweet data into stream to be processed by consumers.  
   Then you will build and deploy stream processing producer application to push tweets into stream.
8. Congratulations!!! You have created your **AWS Kinesis** stream for pushing tweets to be analyzed.
