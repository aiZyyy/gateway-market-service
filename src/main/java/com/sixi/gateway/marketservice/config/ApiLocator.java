package com.sixi.gateway.marketservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

/**
 * @Author: ZY
 * @Date: 2019/8/9 16:01
 * @Version 1.0
 * @Description:
 */
@Slf4j
@EnableAutoConfiguration
@Configuration
public class ApiLocator {

    private static final String SERVICE = "/gateway.do";

    String URI = "http://gateway:8085";

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder serviceProviders = builder.routes()
                .route("PostRoute", r -> r.path(SERVICE)
                        .and()
                        .method(HttpMethod.POST)
                        .filters(r1 -> r1.
                                modifyRequestBody(Object.class, Object.class, (ex, o) -> {
                                    log.info(o.toString());
                                    return Mono.just(o);
                                })).uri(URI)
                        .order(0))
                .route("GetRoute", r -> r.path(SERVICE)
                        .and()
                        .method(HttpMethod.GET)
                        .filters(r2 -> r2.
                                modifyRequestBody(String.class, String.class, (ex, o) -> {
                                    log.info(o);
                                    return Mono.just(o);
                                })).uri(URI)
                        .order(0));
        RouteLocator routeLocator = serviceProviders.build();
        log.info("custom RouteLocator is loading ... {}", routeLocator);
        return routeLocator;
    }
}
