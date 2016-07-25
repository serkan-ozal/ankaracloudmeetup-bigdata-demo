# Ankara Cloud Meetup Big Data Demo - [SEARCHING] Creating AWS Elasticsearch Domain to Index and Search Tweets

1. What is `bigdata-demo-searching-elasticsearch` module?
==============
`bigdata-demo-searching-elasticsearch` explains how to define **AWS Elasticsearch** domain 
where listened tweets will be indexed into to be able to queried later. 

2. Instructions
==============
1. Go to [AWS Elasticsearch](https://console.aws.amazon.com/es) console.
2. Enter name of the search domain (i.e. `bigdata-demo-tweet-search`), 
   where tweets will be indexed, for **Elasticsearch domain name** field and then click **Next**.
3. Configure instance count/type, storage type, etc ... options if you want and then click **Next**.
4. Select **Allow open access to domain** for **Set the domain access policy to** field and then click **Next**. 
   Note that this is not a recommended approach, and should only be used for this demo. 
   Please read the related AWS documentation for how to setup the proper permissions.
5. Review **AWS Elasticsearch** domain configurations and click **Confirm and create** if they are OK. 
6. Your search domain's state will be `Loading` during sometime (~10 minutes) 
   and its state will be updated as `Active` when it is ready to use.
   So wait until search domain's state becomes `Active`.
7. Note the **Endpoint** and **Kibana** URL's of search domain to be used later.   
8. Congratulations!!! You have created your **AWS Elasticsearch** domain for indexing listened and stored tweets.
