package com.example.demo.nacos;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于支持nacos动态刷新 使用@Value注解的属性
 *
 * @Description
 * @Author zhongxing
 * @Date 2019/9/29 14:30
 * @Version 1.0
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RefreshScope {
}


