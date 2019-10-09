package com.example.demo.elk;

import com.lmax.disruptor.EventTranslatorOneArg;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @Description
 * @Author zhongxing
 * @Date 2019/9/2 17:56
 * @Version 1.0
 */

public class LogstashEventTranslator implements EventTranslatorOneArg<LogstashEvent, LoggingEvent> {

    @Override
    public void translateTo(LogstashEvent logstashEvent, long l, LoggingEvent loggingEvent) {
        logstashEvent.setLoggingEvent(loggingEvent);
    }

}


