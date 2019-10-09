package com.example.demo.elk;

import com.lmax.disruptor.EventFactory;

/**
 * @Description
 * @Author zhongxing
 * @Date 2019/9/2 16:49
 * @Version 1.0
 */

public class LogstashEventFactory implements EventFactory<LogstashEvent> {
    @Override
    public LogstashEvent newInstance() {
        return new LogstashEvent();
    }
}


