package tr.com.cloudmeetup.ankara.bigdata.demo.streamprocessing.kinesis.consumer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyseHelper {

    private static final StanfordCoreNLP NLP = new StanfordCoreNLP("nlp.properties");
    
    public static SentimentAnalyseResult doSentimentAnalyze(String text) {
        if (text != null && text.length() > 0) {
            int mainSentiment = 0;
            int longest = 0;
            Annotation annotation = NLP.process(text);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
            }
            if (mainSentiment < 2) {
                return SentimentAnalyseResult.NEGATIVE;
            } else if (mainSentiment > 3) {
                return SentimentAnalyseResult.POSITIVE;
            } else {
                return SentimentAnalyseResult.NEUTRAL;
            }
        } else {
            return SentimentAnalyseResult.NEUTRAL;
        }
    }
    
}
