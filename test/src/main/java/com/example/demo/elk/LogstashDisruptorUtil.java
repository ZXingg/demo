package com.example.demo.elk;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author zhongxing
 * @Date 2019/9/2 16:53
 * @Version 1.0
 */

public class LogstashDisruptorUtil {

    private static final WaitStrategy SLEEPING_WAIT_STRATEGY = new SleepingWaitStrategy();

    private static final int RING_BUFFER_SIZE = 1024;

    private static EventFactory<LogstashEvent> factory = new LogstashEventFactory();

    //private static ThreadPoolExecutor executor;

    private static ThreadFactory threadFactory;

    private static AtomicInteger threadCount = new AtomicInteger(1);

    static {
        threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName("Logstash-Upload-Thread-" + threadCount.getAndIncrement());
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        };
        //executor = new ThreadPoolExecutor(0, 1, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), threadFactory);
    }

    //private static Disruptor<LogstashEvent> disruptor = new Disruptor<>(factory, RING_BUFFER_SIZE, executor, ProducerType.SINGLE, SLEEPING_WAIT_STRATEGY);
    private static Disruptor<LogstashEvent> disruptor = new Disruptor<>(factory, RING_BUFFER_SIZE, threadFactory, ProducerType.SINGLE, SLEEPING_WAIT_STRATEGY);

    private static EventHandler<LogstashEvent> handler = new LogstashEventHandler();

    private static EventTranslatorOneArg<LogstashEvent, LoggingEvent> translator = new LogstashEventTranslator();

    static {
        start();
    }

    private static void start() {
        disruptor.handleEventsWith(handler);
        disruptor.start();
    }

    static void shutdown() {
        disruptor.shutdown();
    }

    static void publish(LoggingEvent loggingEvent) {
        RingBuffer<LogstashEvent> ringBuffer = disruptor.getRingBuffer();

//        long sequence = ringBuffer.next();
//        try {
//            LogstashEvent logstashEvent = ringBuffer.get(sequence);
//            logstashEvent.setLoggingEvent(loggingEvent);
//        } finally {
//            ringBuffer.publish(sequence);
//        }

        ringBuffer.publishEvent(translator, loggingEvent);
    }

}


