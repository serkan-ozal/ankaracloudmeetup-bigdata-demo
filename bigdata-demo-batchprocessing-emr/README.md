# Ankara Cloud Meetup Big Data Demo - [BATCH PROCESSING] Creating Elastic Map-Reduce Cluster

1. What is `bigdata-demo-batchprocessing-emr` module?
==============
`bigdata-demo-batchprocessing-emr` explains how to define **AWS EMR** cluster 
to process stored tweet data as batch.

2. Instructions
==============
1. Go to [AWS Elastic MapReduce](console.aws.amazon.com/elasticmapreduce) console
2. Click **Create cluster** and then you will be redirected to cluster creation page
3. Click **Go to advanced options** at the top of the page, 
   then you will be forwarded to **Step 1: Software and Steps** screen
4. Under **Software Configuration** section, you can select any **Hadoop** distribution (**Amazon** or **MapR**) 
   and specific application such as **Spark**, **Hive**, **Hue**, etc ...
   In here, just be sure that **Hadoop** is selected.
5. Click **Next**, then you will be forwarded to **Step 2: Hardware** screen
6. Select **Master**, **Core** and **Task** node instance types and counts.
   You can also select **Request spot** checkbox to use spot instance for specified instance group.
   In this demo, I used 
  * `m3.xlarge` typed  **Master** node as **Spot** instance with `0.05$` bid price
  * `m3.xlarge` typed  **9 Core** nodes as **Spot** instance with `0.05$` bid price
  * No **Task** nodes
7. Click **Next**, then you will be forwarded to **Step 3: General Cluster Settings** screen
8. Enter name of the cluster whatever you want for **Cluster name** field
9. You can enable/disable logging and specific logging path under **General Options** section
10. Click **Next**, then you will be forwarded to **Step 4: Security** screen
11. You can specify a key pair for **EC2 key pair** field to connect cluster nodes securely via SSH 
    under **Security Options** section
12. Click **Create cluster**, then cluster provisioning and bootstrapping will be started. 
    So wait until cluster's state becomes **ACTIVE**.
13. Then we will be able to run our Hadoop map-reduce job on the cluster for processing stored tweet data as batch.
14. Congratulations!!! You have created your **AWS EMR** cluster to process stored tweet data as batch
