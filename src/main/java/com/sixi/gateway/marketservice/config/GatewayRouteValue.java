package com.sixi.gateway.marketservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Created with IDEA
 *
 * @Description: TODO
 * @Author:zhangchongfei
 * @Date:2019/8/16
 * @Time:12:10
 */
@Component
public class GatewayRouteValue {
    private static final String SERVICE = "/gateway.do";
    @Autowired
    private GatewayConfig gatewayConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    private void saveGateWay() {
        String url = gatewayConfig.url;
        redisTemplate.opsForValue().set(SERVICE, url);
    }

}
