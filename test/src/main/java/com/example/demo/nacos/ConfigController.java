package com.example.demo.nacos;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RefreshScope
@Controller
@RequestMapping("config")
public class ConfigController {

    @NacosInjected
    private ConfigService configService;

    @NacosValue(value = "${useLocalCache:false}", autoRefreshed = true)
    private boolean useLocalCache;

    @Value(value = "${user.name}")
    private String name;

    @NacosValue(value = "${user.name}", autoRefreshed = true)
    private String nacosName;

    @Value(value = "${aaa.name}")
    private String aaaname;

    @NacosValue(value = "${aaa.age}", autoRefreshed = true)
    private String age;

    @RequestMapping(value = "/get", method = GET)
    @ResponseBody
    public String get() {
        log.info("查询配置：" + useLocalCache + " " + name + " " + nacosName + " " + aaaname + " " + age);
//        log.error("测试配置");
        return useLocalCache + " " + name + " " + nacosName + " " + aaaname + " " + age;
    }

    /**
     * curl -X POST 'http://localhost:8080/config?dataId=example&content=useLocalCache=false'
     */
    @RequestMapping(method = POST)
    @ResponseBody
    public ResponseEntity<String> publish(@RequestParam String dataId,
                                          @RequestParam(defaultValue = "DEFAULT_GROUP") String group,
                                          @RequestParam String content) throws NacosException {
        boolean result = configService.publishConfig(dataId, group, content);
        if (result) {
            return new ResponseEntity<String>("Success", HttpStatus.OK);
        }
        return new ResponseEntity<String>("Fail", HttpStatus.INTERNAL_SERVER_ERROR);
    }


}

