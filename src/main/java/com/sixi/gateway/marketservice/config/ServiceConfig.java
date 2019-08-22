package com.sixi.gateway.marketservice.config;

import com.sixi.gateway.marketservice.filter.AuthorizationFilter;
import com.sixi.gateway.marketservice.security.CheckSignServices;
import com.sixi.gateway.marketservice.security.EncapsulationServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

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
    public AuthorizationFilter authorizationFilter(CheckSignServices checkSignServices, EncapsulationServices encapsulationServices) {
        return new AuthorizationFilter(checkSignServices, encapsulationServices);
    }

    @Bean
    @Order(100)
    public CheckSignServices checkSignServices(StringRedisTemplate stringRedisTemplate) {
        return new CheckSignServices(stringRedisTemplate);
    }

    @Bean
    @Order(101)
    public EncapsulationServices EncapsulationServices() {
        return new EncapsulationServices();
    }
}
