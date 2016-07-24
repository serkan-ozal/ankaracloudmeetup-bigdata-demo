package tr.com.cloudmeetup.ankara.bigdata.demo.streamprocessing.kinesis.consumer;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;

public class TweetRecordProcessorFactory implements IRecordProcessorFactory {

    private final String resultTableName;
    
    public TweetRecordProcessorFactory(String resultTableName) {
        this.resultTableName = resultTableName;
    }
    
    @Override
    public IRecordProcessor createProcessor() {
        return new TweeRecordProcessor(resultTableName);
    }

}
