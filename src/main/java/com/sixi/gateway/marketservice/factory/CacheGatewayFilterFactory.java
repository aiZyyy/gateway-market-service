package com.sixi.gateway.marketservice.factory;

import com.sixi.gateway.marketservice.filter.CacheGatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;

import java.util.List;

/**
 * @Author: ZY
 * @Date: 2019/10/17 16:05
 * @Version 1.0
 * @Description:
 */
@Component
public class CacheGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final List<HttpMessageReader<?>> messageReaders;

    public CacheGatewayFilterFactory() {
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return new CacheGatewayFilter();
    }
}
