package tr.com.cloudmeetup.ankara.bigdata.demo.batchprocessing.hadoop;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.log4j.Logger;

/**
 * @author Serkan OZAL
 * 
 * Starts Map/Reduce job. Runs on Master node and submits sub-job to slave nodes (mapper and reducer nodes)
 */
/*
 * AWS EMR URL          : https://console.aws.amazon.com/elasticmapreduce/home?region=us-east-1
 * Spot Instance Prices : http://aws.amazon.com/ec2/purchasing-options/spot-instances/#pricing
 * Instance Type        : m3.xlarge
 * Instance Count       : 1 master + 9 slave nodes
 * Spot Instance Price  : 0.05$
 *
 * Cluster Name         : Ankara Cloud Meetup Big Data Demo - Tweet Sentiment Analyse Batch Processing Hadoop Cluster
 * Jar Location         : s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}/ankaracloudmeetup-bigdata-demo-batchprocessing-hadoop.jar
 * Input Path           : s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-store
 * Output Path          : s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-results/hadoop
 * Log Folder           : s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-logs/hadoop
 * All arguments        : s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-store s3://ankaracloudmeetup-bigdata-demo-${aws.user.accountId}-tweet-batch-results/hadoop [year/month/day]
 */
public class BatchProcessingHadoopDriver {

    private static final Logger LOGGER = Logger.getLogger(BatchProcessingHadoopDriver.class);
    
    private static final DateFormat IO_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    
    private static final boolean GENERATE_ALSO_SINGLE_RESULT_FILE = Boolean.getBoolean("generateSingleResult");
    
    @SuppressWarnings("deprecation")
    public static void main(String[] args) 
            throws IOException, InterruptedException, ClassNotFoundException, ParseException {
        long start, finish;
        long executionTimeInSeconds, executionTimeInMinutes;
        
        //////////////////////////////////////////////////////////////////////////////
        Date analyzeDate = null;
        
        String analyzeDateProp = System.getProperty("analyzeDate");
        if (analyzeDateProp != null) {
            analyzeDate = IO_DATE_FORMAT.parse(analyzeDateProp);
        } else if (args.length > 2) {
            analyzeDate = IO_DATE_FORMAT.parse(args[2]);
        } else { 
            // Find yesterday which is the time that will be analyzed
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 15); //cal.add(Calendar.DATE, -1);
            analyzeDate = cal.getTime();
        }
        
        //////////////////////////////////////////////////////////////////////////////
        
        Properties tweetBatch = PropertiesUtil.getProperties("tweet-batch.properties");
        String resultTableName = tweetBatch.getProperty("aws.tweet.resultTableName");
        
        JobConf conf = new JobConf();
        
        conf.set("resultTableName", resultTableName);
        conf.set("analyseDay", String.valueOf(analyzeDate.getDay()));
        conf.set("analyseMonth", String.valueOf(analyzeDate.getMonth() + 1));
        conf.set("analyseYear", String.valueOf(analyzeDate.getYear() + 1900));

        Job job = new Job(conf, "Ankara Cloud Meetup - Big Data Demo Batch Processing Hadoop Job");

        //////////////////////////////////////////////////////////////////////////////
        
        // Find input path
        String inputDirectoryBase = args[0];
        if (!inputDirectoryBase.endsWith("/")) {
            inputDirectoryBase += "/";
        }
        Path inputPath = new Path(inputDirectoryBase + IO_DATE_FORMAT.format(analyzeDate));
        
        // Find output path
        String outputDirectoryBase = args[1];
        if (!outputDirectoryBase.endsWith("/")) {
            outputDirectoryBase += "/";
        }
        Path outputPath = new Path(outputDirectoryBase + IO_DATE_FORMAT.format(analyzeDate));
        
        //////////////////////////////////////////////////////////////////////////////
        
        FileSystem inputFS = inputPath.getFileSystem(conf);
        
        // If input path is file, use only that files
        if (inputFS.isFile(inputPath)) {
            FileInputFormat.addInputPath(job, inputPath);
        } else {
            // Else, use all files under input directory
            FileStatus[] fileStatusList = inputFS.listStatus(inputPath);
            if (fileStatusList != null) {
                for (FileStatus fileStatus : fileStatusList) {
                    Path filePath = fileStatus.getPath();
                    FileInputFormat.addInputPath(job, filePath);  
                }
            }   
        }
        
        //////////////////////////////////////////////////////////////////////////////
        
        FileSystem outputFS = outputPath.getFileSystem(conf);
        
        while (true) {
            if (outputFS.exists(outputPath)) {
                outputFS.delete(outputPath, true);
                LOGGER.info("Deleting existing output path: " + outputPath.toUri().toString());
                Thread.sleep(1000); 
            } else {
                break;
            }
        }
        
        FileOutputFormat.setOutputPath(job, outputPath);
        
        //////////////////////////////////////////////////////////////////////////////

        job.setJarByClass(BatchProcessingHadoopDriver.class);
        
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);
        
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
         
        job.setMapperClass(SentimentAnalyseMapper.class);
        job.setReducerClass(SentimentAnalyseReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
    
        ////////////////////////////////////////////////////////////////////////////////////

        LOGGER.info("Batch-Processing job has started ...");
        start = System.currentTimeMillis();
        
        // Start job and wait it for completion
        job.waitForCompletion(true);
        
        finish = System.currentTimeMillis();
        executionTimeInSeconds = (finish - start) / 1000;
        executionTimeInMinutes = executionTimeInSeconds / 60;
        LOGGER.info("Batch-Processing job has finished in " + 
                        executionTimeInSeconds + " seconds " + 
                        "(" + executionTimeInMinutes + " minutes" + ")");
        
        ////////////////////////////////////////////////////////////////////////////////////
        
        if (GENERATE_ALSO_SINGLE_RESULT_FILE) {
            Path resultPath = new Path(outputPath + "/result.txt");
            
            LOGGER.info("Batch-Processing output merging has started ...");
            start = System.currentTimeMillis();
    
            // Merge all partitioned output files to single result file such as "output.txt" in output directory.
            FileUtil.copyMerge(outputFS, outputPath, outputFS, resultPath, false, conf, null);
    
            finish = System.currentTimeMillis();
            executionTimeInSeconds = (finish - start) / 1000;
            executionTimeInMinutes = executionTimeInSeconds / 60;
            LOGGER.info("Batch-Processing output merging has finished in " + 
                            executionTimeInSeconds + " seconds " + 
                            "(" + executionTimeInMinutes + " minutes" + ")"); 
        }    
    }

}
