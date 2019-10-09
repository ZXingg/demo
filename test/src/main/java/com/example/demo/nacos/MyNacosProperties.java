package com.example.demo.nacos;


import com.alibaba.nacos.api.annotation.NacosProperties;


/**
 * nacos配置常量
 *
 * @Description
 * @Author zhongxing
 * @Date 2019/9/29 18:17
 * @Version 1.0
 */

public class MyNacosProperties {

    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1:8848";

    public static final String DEFAULT_NAMESPACE = "682434bb-1aff-4a0e-adf2-5b26aa17e96c";

    public static final String DEFAULT_JDBC_DATA_ID = "jdbc.properties";

    public static final String DEFAULT_LOG4J_DATA_ID = "log4j.properties";

    public static final String DEFAULT_ANTI_DS_GROUP_ID_PREFIX = "anti-ds:";

    public static final String DEFAULT_EMS_GROUP_ID = DEFAULT_ANTI_DS_GROUP_ID_PREFIX + "ems";

    public static final String DEFAULT_CENTER_GROUP_ID = DEFAULT_ANTI_DS_GROUP_ID_PREFIX + "center";


    private static final String PREFIX = "${" + NacosProperties.PREFIX;

    private static final String SUFFIX = "}";


    public static final String SERVER_ADDR_PLACEHOLDER_PREFIX = PREFIX + NacosProperties.SERVER_ADDR + ":";

    public static final String NAMESPACE_PLACEHOLDER_PREFIX = PREFIX + NacosProperties.NAMESPACE + ":";

    // DATA_ID、GROUP_ID暂不支持占位符

//    public static final String JDBC_DATA_ID_PLACEHOLDER_PREFIX = PREFIX + "jdbc-data-id:";
//
//    public static final String LOG4J_DATA_ID_PLACEHOLDER_PREFIX = PREFIX + "log4j-data-id:";
//
//    public static final String GROUP_ID_PLACEHOLDER_PREFIX = PREFIX + "group-id:";


    public static final String SERVER_ADDR = SERVER_ADDR_PLACEHOLDER_PREFIX + DEFAULT_SERVER_ADDR + SUFFIX;

    public static final String NAMESPACE = NAMESPACE_PLACEHOLDER_PREFIX + DEFAULT_NAMESPACE + SUFFIX;

//    public static final String JDBC_DATA_ID = JDBC_DATA_ID_PLACEHOLDER_PREFIX + DEFAULT_JDBC_DATA_ID + SUFFIX;
//
//    public static final String LOG4J_DATA_ID = LOG4J_DATA_ID_PLACEHOLDER_PREFIX + DEFAULT_LOG4J_DATA_ID + SUFFIX;
//
//    public static final String EMS_GROUP_ID = GROUP_ID_PLACEHOLDER_PREFIX + DEFAULT_ANTI_DS_GROUP_ID_PREFIX + "ems" + SUFFIX;
//
//    public static final String CENTER_GROUP_ID = GROUP_ID_PLACEHOLDER_PREFIX + DEFAULT_ANTI_DS_GROUP_ID_PREFIX + "center" + SUFFIX;


}


