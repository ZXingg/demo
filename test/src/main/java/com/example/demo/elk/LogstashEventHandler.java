package com.example.demo.elk;

import com.alibaba.fastjson.JSONObject;
import com.lmax.disruptor.EventHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.example.demo.elk.JsonEventLayout.applicationProperties;

/**
 * @Description ELK异步日志处理
 * @Author zhongxing
 * @Date 2019/9/2 16:50
 * @Version 1.0
 */

public class LogstashEventHandler implements EventHandler<LogstashEvent> {

    private LogstashSocketAppender socketAppender;

    private JsonEventLayout layout;

    {
        Logger rootLogger = Logger.getRootLogger();
        socketAppender = (LogstashSocketAppender) rootLogger.getAppender("logstash");
        if (socketAppender == null) {
            LogstashDisruptorUtil.shutdown();
        } else {
            layout = (JsonEventLayout) socketAppender.getLayout();
        }

//        本地开发环境不使用ELK
//        if (applicationProperties == null) {
//            applicationProperties = new Properties();
//            try {
//                InputStream inputStream = new ClassPathResource(layout.configFilePath).getInputStream();
//                applicationProperties.load(inputStream);
//            } catch (IOException e) {
//                LogLog.error(JsonEventLayout.THIS_CLASS + "load jdbc.properties fail. errMsg=" + e.getMessage());
//            }
//        }
//
//        String env = applicationProperties.getProperty("env");
//
//        if (StringUtils.isEmpty(env)) {
//            ignore();
//        } else {
//
//            boolean ignorable = true;
//            String[] envs = layout.allowableEnvironment.split(",", 2);
//
//            if (ObjectUtils.isEmpty(envs)) {
//                ignore();
//            } else {
//                for (String e : envs) {
//                    if (env.trim().equals(e.trim())) {
//                        ignorable = false;
//                        break;
//                    }
//                }
//
//                if (ignorable) {
//                    ignore();
//                }
//            }
//
//        }

    }

//    private void ignore() {
//        LogLog.warn("Service environment not match any logstash environment, LogstashSocketAppender will ignore the log");
//        rootLogger.removeAppender(socketAppender);
//        LogstashDisruptorUtil.shutdown();
//    }

    @Override
    public void onEvent(LogstashEvent logstashEvent, long l, boolean b) throws Exception {
        String data = format(logstashEvent.getLoggingEvent());
        socketAppender.sendEvent(data);
    }

    private String format(LoggingEvent loggingEvent) {
        Map<String, Object> exceptionInformation = new HashMap<>(4, 1);
        layout.logstashEvent = new JSONObject(16);

        layout.logstashEvent.put("ip", layout.ip);
        layout.logstashEvent.put("hostname", layout.hostname);
        long timeStamp = loggingEvent.getTimeStamp();
        layout.logstashEvent.put("datetime", JsonEventLayout.dateFormat(timeStamp));
        layout.addEventData("thread", loggingEvent.getThreadName());
        layout.addEventData("level", loggingEvent.getLevel().toString());
        layout.logstashEvent.put("message", loggingEvent.getRenderedMessage());

        if (layout.enableLocationInfo) {
            LocationInfo locationInfo = loggingEvent.getLocationInformation();
            String caller = locationInfo.getClassName() + "." + locationInfo.getMethodName() + "." + locationInfo.getLineNumber();
            layout.addEventData("caller", caller);
        }

        layout.addEventData("logger", loggingEvent.getLoggerName());
        layout.addEventData("mdc", loggingEvent.getProperties());
        layout.addEventData("ndc", loggingEvent.getNDC());

        //添加用户自定义属性-spring配置文件中的属性将会覆盖log4j配置的用户属性
        if (layout.userFields != null) {
            LogLog.debug("[" + JsonEventLayout.THIS_CLASS + "] Got user data from log4j configuration file, property: " + layout.userFields);
            layout.addUserFields(layout.userFields);
        }

        if (applicationProperties == null) {
            applicationProperties = new Properties();
            try {
                InputStream inputStream = new ClassPathResource(layout.configFilePath).getInputStream();
                applicationProperties.load(inputStream);
            } catch (IOException e) {
                LogLog.error(JsonEventLayout.THIS_CLASS + "load jdbc.properties fail. errMsg=" + e.getMessage());
            }
        }

        String userFieldsProperty = applicationProperties.getProperty(JsonEventLayout.USER_FIELDS_KEY);
        if (userFieldsProperty != null) {
            if (layout.userFields != null) {
                LogLog.warn("[" + JsonEventLayout.THIS_CLASS + "] Loading UserFields from configuration file (" + layout.configFilePath + "). layout will override repetitive UserFields key set in the log4j configuration file");
            }
            LogLog.debug("[" + JsonEventLayout.THIS_CLASS + "] Got user data from " + layout.configFilePath + " configuration file, property: " + userFieldsProperty);
            layout.addUserFields(userFieldsProperty);
        }

        //添加异常信息
        if (loggingEvent.getThrowableInformation() != null) {
            ThrowableInformation throwableInformation = loggingEvent.getThrowableInformation();

            if (throwableInformation.getThrowable().getClass().getCanonicalName() != null) {
                exceptionInformation.put("exception_class", throwableInformation.getThrowable().getClass().getCanonicalName());
            }

            if (throwableInformation.getThrowable().getMessage() != null) {
                exceptionInformation.put("exception_message", throwableInformation.getThrowable().getMessage());
            }

            if (throwableInformation.getThrowableStrRep() != null) {
                String stackTrace = StringUtils.join(throwableInformation.getThrowableStrRep(), "\n");
                exceptionInformation.put("stacktrace", stackTrace);
            }

            layout.addEventData("exception", exceptionInformation);
        }

        return layout.logstashEvent.toString() + "\n";
    }
}


