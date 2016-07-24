package tr.com.cloudmeetup.ankara.bigdata.demo.batchprocessing.hadoop;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class SentimentAnalyseMapper extends Mapper<LongWritable, Text, IntWritable, Text> {

    private static final DateFormat TWEET_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static final Pattern STR_NORMALIZATION_PATTERN = Pattern.compile("\t|\\r|\\n|\\r\\n");
    private static final Matcher STR_NORMALIZATION_MATCHER = STR_NORMALIZATION_PATTERN.matcher("");
    
    @Override
    protected void map(LongWritable key, Text value,
                       Mapper<LongWritable, Text, IntWritable, Text>.Context context)
            throws IOException, InterruptedException {
        String tweetJsonData = value.toString();
        JSONObject tweetJsonObj = new JSONObject(tweetJsonData);
        String user = tweetJsonObj.getJSONObject("user").getString("screen_name");
        String tweet = normalizeString(tweetJsonObj.getString("text"));
        Date date = new Date(tweetJsonObj.getString("created_at"));
        int hour = date.getHours();
        SentimentAnalyseResult sentimentAnalyseResult = SentimentAnalyseHelper.doSentimentAnalyze(tweet);
        // TSV format delimited by tabs
        String analyzedTweetData = 
                user + "\t" + 
                tweet + "\t" +  
                TWEET_DATE_FORMAT.format(date) + "\t" + 
                sentimentAnalyseResult.name();
        
        context.write(new IntWritable(hour), new Text(analyzedTweetData));
    }
    
    private static String normalizeString(String str) {
        return STR_NORMALIZATION_MATCHER.reset(str).replaceAll(" ");
    }

}
