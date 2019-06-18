package edu.msu.mi.mturk_utils

import com.amazonaws.mturk.util.ClientConfig
import org.apache.log4j.Logger

/**
 * Created by josh on 1/27/16.
 */
/**
 * User: jintrone
 * Date: 9/28/12
 * Time: 1:50 PM
 */
public class FilePropertiesConfig extends ClientConfig{

    public final static String ACCESS_KEY_ID = "access_key";
    public final static String SECRET_ACCESS_KEY = "secret_key";
    public final static String SERVICE_URL = "service_url";
    public final static String LOG_LEVEL = "log_level";
    public final static String RETRY_ATTEMPTS = "retry_attempts";
    public final static String RETRY_DELAY_MILLIS = "retry_delay_millis";
    public final static String RETRIABLE_ERRORS = "retriable_errors";

    public final static String NOT_CONFIGURED_PREFIX = "[insert";
    public final static String NOT_CONFIGURED_POSTFIX = "]";
    public final static String SANDBOX = "sandbox";

    private static Logger log = Logger.getLogger(FilePropertiesConfig.class);

    private static boolean isNotConfigured(String propVal) {
        // avoid values that are obviously not configured by the user to be
        // handled as a valid value (e.g. "[insert your access key here]")
        return propVal == null ||
                (propVal.startsWith(NOT_CONFIGURED_PREFIX) &&
                        propVal.endsWith(NOT_CONFIGURED_POSTFIX));
    }

    private static String getTrimmedProperty(String propName, Properties props,  String failsafe) {
        String prop = props.getProperty(propName);
        if (isNotConfigured(prop)) {
            prop = failsafe;
        }
        if (prop == null) {
            return null;
        }
        return prop.trim();
    }






    public FilePropertiesConfig(String propertiesFilename) throws IOException {
        this(new FileInputStream(propertiesFilename));

    }

    public FilePropertiesConfig(InputStream stream) throws IOException {
        super();


        Properties props = new java.util.Properties();
        props.load(stream);

        if (getTrimmedProperty(SANDBOX,props,"true").equals("false")) {
            setServiceURL(PRODUCTION_SERVICE_URL)
        }

        setAccessKeyId(getTrimmedProperty(ACCESS_KEY_ID, props,  getAccessKeyId()));
        setSecretAccessKey(getTrimmedProperty(SECRET_ACCESS_KEY, props, getSecretAccessKey()));
        setServiceURL(getTrimmedProperty(SERVICE_URL, props,  getServiceURL()));

        // optional settings
        setLogLevel(getTrimmedProperty(LOG_LEVEL, props, null));
        String retryAttemptsProp = getTrimmedProperty(RETRY_ATTEMPTS, props, null);
        setRetryAttempts(retryAttemptsProp != null ? Integer.parseInt(retryAttemptsProp) : 3);

        String retryDelayProp = getTrimmedProperty(RETRY_DELAY_MILLIS, props,null);
        setRetryDelayMillis(retryDelayProp != null ? Long.parseLong(retryDelayProp) : 500);

        String errorsProp = getTrimmedProperty(RETRIABLE_ERRORS, props,null);
        String[] errors = errorsProp != null ? errorsProp.split(",") : new String[0];
        Set<String> retriableErrors = new HashSet<String>();
        for (String error : errors) {
            retriableErrors.add(error.trim());
        }
        setRetriableErrors(retriableErrors);

    }
}