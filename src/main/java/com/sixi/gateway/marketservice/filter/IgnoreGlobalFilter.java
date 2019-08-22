//package com.sixi.gateway.marketservice.filter;
//
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
///**
// * @Author: ZY
// * @Date: 2019/8/22 17:46
// * @Version 1.0
// * @Description: 跳过检测
// */
//public class IgnoreGlobalFilter extends AbstractGatewayFilterFactory<IgnoreGlobalFilter.Config> {
//
//    public IgnoreGlobalFilter() {
//        super(Config.class);
//    }
//
//    @Override
//    public GatewayFilter apply(Config config) {
//        return this::filter;
//    }
//
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        exchange.getAttributes().put(AuthorizationFilter.ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER, true);
//        return chain.filter(exchange);
//    }
//
//    public static class Config {
//
//    }
//
//    @Override
//    public String name() {
//        return "IgnoreGlobalFilter";
//    }
//}
