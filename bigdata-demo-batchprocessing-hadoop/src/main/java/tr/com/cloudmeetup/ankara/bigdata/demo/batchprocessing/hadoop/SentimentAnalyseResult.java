package tr.com.cloudmeetup.ankara.bigdata.demo.batchprocessing.hadoop;

public enum SentimentAnalyseResult {

    NEGATIVE(-1),
    NEUTRAL(0),
    POSITIVE(+1);
    
    private final int code;
    
    SentimentAnalyseResult(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
    
    public static SentimentAnalyseResult getSentimentAnalyseResult(int code) {
        for (SentimentAnalyseResult result : values()) {
            if (result.code == code) {
                return result;
            }
        }
        throw new IllegalArgumentException("Invalid code for SentimentAnalyseResult: " + code);
    }

}
