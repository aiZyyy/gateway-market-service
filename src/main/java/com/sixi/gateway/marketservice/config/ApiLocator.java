package com.sixi.gateway.marketservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .route("Route", r -> r.path(SERVICE)
                        .and()
                        .method(HttpMethod.POST)
                        .filters(r1 -> r1.
                                modifyRequestBody(String.class, String.class, (ex, o) -> {
                                    System.err.println(o.toUpperCase());
                                    log.info(o);
                                    return Mono.just(o);
                                })).uri(URI));
        RouteLocator routeLocator = serviceProviders.build();
        log.info("custom RouteLocator is loading ... {}", routeLocator);
        return routeLocator;
    }
}
