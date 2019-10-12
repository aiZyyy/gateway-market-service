package com.sixi.gateway.marketservice.config;

import com.sixi.gateway.marketservice.filter.AuthorizationFilter;
import com.sixi.gateway.marketservice.security.AuthBodyServices;
import com.sixi.gateway.marketservice.security.CheckSignServices;
import com.sixi.gateway.marketservice.security.EncapsulationServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Author: ZY
 * @Date: 2019/8/22 10:54
 * @Version 1.0
 * @Description:
 */
@Configuration
@EnableConfigurationProperties
public class ServiceConfig {


    @Bean
    @Order(1)
    public AuthorizationFilter authorizationFilter(CheckSignServices checkSignServices, AuthBodyServices authBodyServices,EncapsulationServices encapsulationServices) {
        return new AuthorizationFilter(checkSignServices, authBodyServices,encapsulationServices);
    }

    @Bean
    @Order(100)
    public AuthBodyServices authBodyServices() {
        return new AuthBodyServices();
    }

    @Bean
    @Order(101)
    public CheckSignServices checkSignServices(StringRedisTemplate stringRedisTemplate) {
        return new CheckSignServices(stringRedisTemplate);
    }

    @Bean
    @Order(102)
    public EncapsulationServices encapsulationServices() {
        return new EncapsulationServices();
    }

}
