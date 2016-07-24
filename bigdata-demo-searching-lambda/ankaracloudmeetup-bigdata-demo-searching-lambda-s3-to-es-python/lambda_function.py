'''
Created on Oct 8, 2015
@author: mentzera

Modified on Jul 1, 2016
@author: sozal
'''


import json
import boto3
import twitter_to_es


es_port = 80
es_host = ''
s3 = boto3.client('s3')
lmbd = boto3.client('lambda')


def lambda_handler(event, context):
    print('Received event: ' + json.dumps(event, indent=2))

    # Get the object from the event and show its content type
    bucket = event['Records'][0]['s3']['bucket']['name']
    key = event['Records'][0]['s3']['object']['key']

    # Get elastic search endpoint from the description of lambda function
    config = lmbd.get_function_configuration(FunctionName=context.function_name)
    description = json.loads(config['Description'])
    es_host = description['aws.search.domainEndpoint']

    # Getting s3 object
    try:
        response = s3.get_object(Bucket=bucket, Key=key)
              
    except Exception as e:
        print(e)
        print('Error getting object {} from bucket {}. Make sure they exist and your bucket is in the same region as this function.'.format(key, bucket))
        raise e
    
    # Parse s3 object content (JSON)
    try:
        # Get content of uploaded file to S3
        s3_file_content = response['Body'].read()
        # Remove last new line character
        if s3_file_content.endswith('\n'):
            s3_file_content = s3_file_content[:-1]
        # Replace new line characters with comma character.
        s3_file_content = s3_file_content.replace('\n', ',')
        # Represent all tweets as array of tweet items in JSON
        tweets_str = '[' + s3_file_content + ']'
        # Parse and load tweets from JSON
        tweets = json.loads(tweets_str)

    except Exception as e:
        print(e)
        print('Error loading json from object {} in bucket {}'.format(key, bucket))
        raise e
    
    # Save data into ES
    try:
        twitter_to_es.save(tweets, es_host, es_port)

    except Exception as e:
        print(e)
        print('Error loading data into ElasticSearch')
        raise e    

