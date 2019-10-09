package com.example.demo.nacos;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.spring.context.annotation.config.EnableNacosConfig;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import org.springframework.context.annotation.Configuration;

/**
 * nacos配置
 *
 * @Description
 * @Author zhongxing
 * @Date 2019/9/29 14:30
 * @Version 1.0
 */

@Configuration
@EnableNacosConfig(globalProperties = @NacosProperties(serverAddr = MyNacosProperties.SERVER_ADDR, namespace = MyNacosProperties.NAMESPACE))
@NacosPropertySources({

        @NacosPropertySource(dataId = MyNacosProperties.DEFAULT_JDBC_DATA_ID, groupId = MyNacosProperties.DEFAULT_EMS_GROUP_ID, autoRefreshed = true),
        //before = StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
        //@NacosPropertySource(dataId = MyNacosProperties.DEFAULT_LOG4J_DATA_ID, groupId = MyNacosProperties.DEFAULT_EMS_GROUP_ID, first = true),

})
public class MyNacosConfiguration {

}