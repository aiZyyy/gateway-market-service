package com.sixi.gateway.marketservice.config;

import com.sixi.gateway.marketservice.handler.FallbackHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @Author: ZY
 * @Date: 2019/8/22 11:03
 * @Version 1.0
 * @Description:
 */
@Configuration
@EnableWebFlux
public class WebfluxRouteConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(FallbackHandler fallbackHandler) {
        return route(GET("/fallbackcontroller"), fallbackHandler::all)
                .andRoute(POST("/fallbackcontroller"), fallbackHandler::all);
    }
}
