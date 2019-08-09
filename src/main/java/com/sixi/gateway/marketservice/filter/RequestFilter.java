package com.sixi.gateway.marketservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author: ZY
 * @Date: 2019/8/9 15:59
 * @Version 1.0
 * @Description:
 */
@Component
@Slf4j
public class RequestFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Object requestBody = exchange.getAttribute("cachedRequestBodyObject");
        log.info("request body is:{}", requestBody);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            System.out.println("RequestFilter post filter");
        }));
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
