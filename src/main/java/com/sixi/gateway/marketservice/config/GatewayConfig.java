package com.sixi.gateway.marketservice.config;

import com.sixi.gateway.marketservice.filter.OauthFilter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @Author: ZY
 * @Date: 2019/8/9 14:52
 * @Version 1.0
 * @Description:
 */
@SpringBootConfiguration
public class GatewayConfig {

    @Bean
    public OauthFilter tokenFilter(){
        return new OauthFilter();
    }
}
