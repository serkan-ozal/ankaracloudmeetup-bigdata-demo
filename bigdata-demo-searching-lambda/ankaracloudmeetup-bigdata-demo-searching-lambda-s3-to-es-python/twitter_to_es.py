'''
Created on Oct 8, 2015
@author: mentzera

Modified on Jul 1, 2016
@author: sozal
'''


from elasticsearch import Elasticsearch
from elasticsearch.helpers import bulk
from elasticsearch.exceptions import ElasticsearchException
from tweet_utils import analyze_and_get_tweet, id_field, tweet_mapping


index_name = 'twitter'
doc_type = 'tweet'
mapping = {
    doc_type: tweet_mapping
}
bulk_chunk_size = 1000


def save(tweets, es_host, es_port):    
    es = Elasticsearch(host = es_host, port = es_port)

    print('Saving tweets into ElasticSearch on {}...'.format(es_host))

    if es.indices.exists(index_name):
        print ('Index {} already exists'.format(index_name))
        try:
            es.indices.put_mapping(doc_type, tweet_mapping, index_name)
        except ElasticsearchException as e:
            print('Error while putting mapping:\n' + str(e))
            print('Deleting index {} on...'.format(index_name))
            es.indices.delete(index_name)
            print('Creating index {}...'.format(index_name))
            es.indices.create(index_name, body = {'mappings': mapping})
    else:
        print('Index {} does not exist'.format(index_name))
        print('Creating index {}...'.format(index_name))
        es.indices.create(index_name, body = {'mappings': mapping})

    counter = 0
    bulk_data = []
    list_size = len(tweets)
    for doc in tweets:
        tweet = analyze_and_get_tweet(doc)
        bulk_doc = {
            "_index": index_name,
            "_type": doc_type,
            "_id": tweet[id_field],
            "_source": tweet
        }
        bulk_data.append(bulk_doc)
        counter += 1

        if counter % bulk_chunk_size == 0 or counter == list_size:
            print('ElasticSearch bulk index (index: {INDEX}, type: {TYPE})...'.format(INDEX=index_name, TYPE=doc_type))
            success, _ = bulk(es, bulk_data)
            print 'ElasticSearch has indexed %d documents' % success
            bulk_data = []

