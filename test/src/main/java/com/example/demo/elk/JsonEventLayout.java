package com.example.demo.elk;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import java.net.InetAddress;
import java.time.ZoneId;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @Description
 * @Author zhongxing
 * @Date 2019/8/28 10:20
 * @Version 1.0
 */

public class JsonEventLayout extends Layout {

    public boolean ignoreThrowable;
    public boolean enableLocationInfo;
    public String allowableEnvironment;
    public String userFields;
    public String configFilePath;
    public String hostname;
    public String ip;
    public JSONObject logstashEvent;

    public static Properties applicationProperties;
    public static final String USER_FIELDS_KEY = "logstash.userFields";
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai"));
    private static final FastDateFormat DATETIME_FORMAT;
    public static final String THIS_CLASS = JsonEventLayout.class.getSimpleName();

    static {
        DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", TIME_ZONE);
    }

    public static String dateFormat(long timestamp) {
        return DATETIME_FORMAT.format(timestamp);
    }

    public JsonEventLayout() {
        this(true);
    }

    @SneakyThrows
    private JsonEventLayout(boolean enableLocationInfo) {
        this.enableLocationInfo = enableLocationInfo;
        this.ignoreThrowable = false;
        this.hostname = InetAddress.getLocalHost().getHostName();
        this.ip = InetAddress.getLocalHost().getHostAddress();
    }

    @Override
    public String format(LoggingEvent loggingEvent) {
        //交由disruptor处理
        LogstashDisruptorUtil.publish(loggingEvent);

        return "";
//        Map<String, Object> exceptionInformation = new HashMap<>(4, 1);
//        logstashEvent = new JSONObject(16);
//
//        this.logstashEvent.put("ip", this.ip);
//        this.logstashEvent.put("hostname", this.hostname);
//        long timeStamp = loggingEvent.getTimeStamp();
//        this.logstashEvent.put("datetime", dateFormat(timeStamp));
//        this.addEventData("thread", loggingEvent.getThreadName());
//        this.addEventData("level", loggingEvent.getLevel().toString());
//        this.logstashEvent.put("message", loggingEvent.getRenderedMessage());
//
//        if (this.enableLocationInfo) {
//            LocationInfo locationInfo = loggingEvent.getLocationInformation();
//            String caller = locationInfo.getClassName() + "." + locationInfo.getMethodName() + "." + locationInfo.getLineNumber();
//            this.addEventData("caller", caller);
//        }
//
//        this.addEventData("logger", loggingEvent.getLoggerName());
//        this.addEventData("mdc", loggingEvent.getProperties());
//        this.addEventData("ndc", loggingEvent.getNDC());
//
//        //添加用户自定义属性-spring配置文件中的属性将会覆盖log4j配置的用户属性
//        if (this.userFields != null) {
//            LogLog.debug("[" + THIS_CLASS + "] Got user data from log4j configuration file, property: " + this.userFields);
//            this.addUserFields(this.userFields);
//        }
//
//        if (applicationProperties == null) {
//            applicationProperties = new Properties();
//            try {
//                InputStream inputStream = new ClassPathResource(configFilePath).getInputStream();
//                applicationProperties.load(inputStream);
//            } catch (IOException e) {
//                LogLog.error(JsonEventLayout.class.getSimpleName() + "load jdbc.properties fail. errMsg=" + e.getMessage());
//            }
//        }
//
//        String userFieldsProperty = applicationProperties.getProperty(USER_FIELDS_KEY);
//        if (userFieldsProperty != null) {
//            if (this.userFields != null) {
//                LogLog.warn("[" + THIS_CLASS + "] Loading UserFields from configuration file (" + configFilePath + "). This will override repetitive UserFields key set in the log4j configuration file");
//            }
//            LogLog.debug("[" + THIS_CLASS + "] Got user data from " + configFilePath + "configuration file, property: " + userFieldsProperty);
//            this.addUserFields(userFieldsProperty);
//        }
//
//        //添加异常信息
//        if (loggingEvent.getThrowableInformation() != null) {
//            ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();
//
//            if (throwableInformation.getThrowable().getClass().getCanonicalName() != null) {
//                exceptionInformation.put("exception_class", throwableInformation.getThrowable().getClass().getCanonicalName());
//            }
//
//            if (throwableInformation.getThrowable().getMessage() != null) {
//                exceptionInformation.put("exception_message", throwableInformation.getThrowable().getMessage());
//            }
//
//            if (throwableInformation.getThrowableStrRep() != null) {
//                String stackTrace = StringUtils.join(throwableInformation.getThrowableStrRep(), "\n");
//                exceptionInformation.put("stacktrace", stackTrace);
//            }
//
//            this.addEventData("exception", exceptionInformation);
//        }
//
//        return this.logstashEvent.toString() + "\n";
    }

    @Override
    public boolean ignoresThrowable() {
        return this.ignoreThrowable;
    }

    @Override
    public void activateOptions() {
    }

    public void addUserFields(String data) {
        if (null != data) {
            String[] pairs = data.split(",");
            for (String pair : pairs) {
                String[] userField = pair.split(":", 2);
                if (userField[0] != null) {
                    String key = userField[0].trim();
                    String val = userField[1].trim();
                    this.addEventData(key, val);
                }
            }
        }
    }

    public void addEventData(String key, Object val) {
        if (null != val) {
            this.logstashEvent.put(key.trim(), val);
        }
    }

    public boolean isIgnoreThrowable() {
        return ignoreThrowable;
    }

    public void setIgnoreThrowable(boolean ignoreThrowable) {
        this.ignoreThrowable = ignoreThrowable;
    }

    public boolean isEnableLocationInfo() {
        return enableLocationInfo;
    }

    public void setEnableLocationInfo(boolean enableLocationInfo) {
        this.enableLocationInfo = enableLocationInfo;
    }

    public String getUserFields() {
        return userFields;
    }

    public void setUserFields(String userFields) {
        this.userFields = userFields;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getAllowableEnvironment() {
        return allowableEnvironment;
    }

    public void setAllowableEnvironment(String allowableEnvironment) {
        this.allowableEnvironment = allowableEnvironment;
    }
}


