package com.sixi.gateway.marketservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created with IDEA
 *
 * @Description: TODO
 * @Author:zhangchongfei
 * @Date:2019/8/16
 * @Time:12:10
 */
@Slf4j
@Component
public class GatewayRouteValue {
    private static final String key = "gateway:/gateway.do";
    @Autowired
    private GatewayConfig gatewayConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    private void saveGateWay() {
        log.info("开始加载GatewayConfig到redis=========");
        String url = gatewayConfig.url;
        redisTemplate.opsForValue().set(key, url);
        log.info("加载GatewayConfig到redis完成=========");
    }

}
