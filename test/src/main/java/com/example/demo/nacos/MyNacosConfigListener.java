package com.example.demo.nacos;

import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.helpers.OptionConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * nacos配置更新监听 增强-异步更新@RefreshScope bean中使用了@Value注解的属性
 *
 * @Description
 * @Author zhongxing
 * @Date 2019/9/29 14:35
 * @Version 1.0
 */

@Slf4j
@Configuration
public class MyNacosConfigListener implements ApplicationContextAware {

    @AllArgsConstructor
    @Setter
    @Getter
    private
    class DynamicUpdateBean {
        private Object target;
        private String fieldName;
        private String fieldValue;
    }

    private static Map<String, List<DynamicUpdateBean>> keyObjectFieldMap = new HashMap<>();

    //@NacosConfigListener(dataId = MyNacosProperties.DEFAULT_JDBC_DATA_ID, groupId = MyNacosProperties.DEFAULT_EMS_GROUP_ID, timeout = 10000, converter = MyPropertiesNacosConfigConverter.class)
    @NacosConfigListener(dataId = MyNacosProperties.DEFAULT_JDBC_DATA_ID, groupId = MyNacosProperties.DEFAULT_EMS_GROUP_ID, timeout = 10000)
    public void onReceived(Properties properties) {

        log.info("onReceived(Properties) : {}", properties);

        CompletableFuture.runAsync(() -> {

            long startTime = System.currentTimeMillis();

            properties.forEach((key, value) -> {

                List<DynamicUpdateBean> beanList = keyObjectFieldMap.get(String.valueOf(key));

                if (CollectionUtils.isEmpty(beanList)) {
                    return;
                }

                beanList.forEach(bean -> {

                    String varValue = substVars(String.valueOf(value), properties);

                    if (bean.fieldValue != null && bean.fieldValue.equals(varValue)) {
                        return;
                    }

                    try {
                        Class<?> aClass = bean.target.getClass();
                        Field field = aClass.getDeclaredField(bean.fieldName);

                        field.setAccessible(true);
                        Object oldValue = field.get(bean.target);
                        field.set(bean.target, varValue);

                        log.info("Dynamic update {}.{}: {} => {}.", aClass.getName(), field.getName(), oldValue, varValue);
                    } catch (NoSuchFieldException | IllegalAccessException ex) {
                        log.error("Dynamic update of an field using the @Value annotation failed!", ex);
                    }

                });

            });

            log.info("Dynamic update of the field using the @Value annotation spend {}ms", System.currentTimeMillis() - startTime);
        });

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        CompletableFuture.runAsync(() -> {

            long startTime = System.currentTimeMillis();

            Map<String, Object> beansMap = applicationContext.getBeansWithAnnotation(RefreshScope.class);
            log.info("The number of bean with @RefreshScope annotations is {}.", beansMap.size());

            for (Object value : beansMap.values()) {

                Class<?> aClass = value.getClass();
                Field[] fields = aClass.getDeclaredFields();

                for (Field field : fields) {

                    Value annotation = field.getAnnotation(Value.class);
                    if (annotation == null) {
                        continue;
                    }

                    String aValue = annotation.value();
                    if (StringUtils.isNotBlank(aValue) && aValue.startsWith("${") && aValue.endsWith("}")) {

                        int index = aValue.indexOf(":");
                        String varName = index == -1 ? aValue.substring(2, aValue.length() - 1) : aValue.substring(2, index);
                        List<DynamicUpdateBean> beanList = keyObjectFieldMap.get(varName);

                        field.setAccessible(true);
                        Object fieldValue = null;
                        try {
                            fieldValue = field.get(value);
                        } catch (IllegalAccessException e) {
                            log.error("An exception occurred while getting the bean annotated with @RefreshScope.", e);
                        }

                        if (CollectionUtils.isEmpty(beanList)) {
                            Object finalValue = fieldValue;
                            keyObjectFieldMap.put(varName, new ArrayList<DynamicUpdateBean>() {{
                                add(new DynamicUpdateBean(value, field.getName(), String.valueOf(finalValue)));
                            }});
                        } else {
                            beanList.add(new DynamicUpdateBean(value, field.getName(), String.valueOf(fieldValue)));
                        }

                    }

                }

            }

            log.info("Get the bean annotated with @RefreshScope spend " + (System.currentTimeMillis() - startTime) + "ms");
            log.info("The number of field(key) with @Value annotations that need to be updated dynamically is {}.", keyObjectFieldMap.size());

        });

    }

    private static final String DELIM_START = "${";
    private static final char DELIM_STOP = '}';
    private static final int DELIM_START_LEN = 2;
    private static final int DELIM_STOP_LEN = 1;

    /**
     * 解析变量（aaa ${var} bbb）的值，支持变量嵌套，注意：若属性使用的@NacosValue注解则不支持变量嵌套解析
     * 变量获取顺序与 {@link OptionConverter}.substVars不同，先从props获取，若没有再从system获取
     *
     * @param val   变量字符串
     * @param props properties
     * @return 解析后的值
     * @throws IllegalArgumentException 占位符错误
     */
    public static String substVars(String val, Properties props) throws
            IllegalArgumentException {

        StringBuilder sb = new StringBuilder();

        int i = 0;
        int j, k;

        while (true) {
            j = val.indexOf(DELIM_START, i);
            if (j == -1) {
                // no more variables
                if (i == 0) {
                    // this is a simple string
                    return val;
                } else { // add the tail string which contails no variables and return the result.
                    sb.append(val.substring(i));
                    return sb.toString();
                }
            } else {
                sb.append(val, i, j);
                k = val.indexOf(DELIM_STOP, j);
                if (k == -1) {
                    throw new IllegalArgumentException('"' + val +
                            "\" has no closing brace. Opening brace at position " + j
                            + '.');
                } else {
                    j += DELIM_START_LEN;
                    String key = val.substring(j, k);
                    // first try in props parameter (Different with org.apache.log4j.helpers.OptionConverter order)
                    String replacement = null;
                    if (props != null) {
                        replacement = props.getProperty(key);
                    }
                    // then try System properties
                    if (replacement == null) {
                        replacement = OptionConverter.getSystemProperty(key, null);
                    }

                    if (replacement != null) {
                        // Do variable substitution on the replacement string
                        // such that we can solve "Hello ${x2}" as "Hello p1"
                        // the where the properties are
                        // x1=p1
                        // x2=${x1}
                        String recursiveReplacement = substVars(replacement, props);
                        sb.append(recursiveReplacement);
                    }
                    i = k + DELIM_STOP_LEN;
                }
            }
        }
    }

}
