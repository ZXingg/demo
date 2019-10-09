package com.example.demo.nacos;

import com.alibaba.nacos.api.config.convert.NacosConfigConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Properties;

/**
 * 配置解析转换器
 *
 * @Description
 * @Author zhongxing
 * @Date 2019/9/29 14:40
 * @Version 1.0
 */

public class MyPropertiesNacosConfigConverter implements NacosConfigConverter<Properties> {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canConvert(Class<Properties> targetType) {
        return objectMapper.canSerialize(targetType);
    }

    @Override
    public Properties convert(String config) {
        try {
            return objectMapper.readValue(config, Properties.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
