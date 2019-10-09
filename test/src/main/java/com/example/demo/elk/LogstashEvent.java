package com.example.demo.elk;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.log4j.spi.LoggingEvent;

import java.io.Serializable;

/**
 * @Description
 * @Author zhongxing
 * @Date 2019/8/28 10:07
 * @Version 1.0
 */
@Data
@NoArgsConstructor
public class LogstashEvent implements Serializable {

    private LoggingEvent loggingEvent;

}


