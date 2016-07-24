package tr.com.cloudmeetup.ankara.bigdata.demo.batchprocessing.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class PropertiesUtil {

    private static final Logger LOGGER = Logger.getLogger(PropertiesUtil.class);
    
    private PropertiesUtil() {
        
    }
    
    public static Properties getProperties(String propFileName) throws IOException {
        Properties props = new Properties();
        try {
            InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream(propFileName);
            if (in != null) {
                props.load(in);
            } 
            props.putAll(System.getProperties());
            return props;
        } catch (IOException e) {
            LOGGER.error("Error occured while loading properties from " + "'" + propFileName + "'", e);
            throw e;
        }
    }

}
