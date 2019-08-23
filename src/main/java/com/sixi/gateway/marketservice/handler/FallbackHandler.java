package com.sixi.gateway.marketservice.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @Author: ZY
 * @Date: 2019/8/22 11:04
 * @Version 1.0
 * @Description:
 */
@Component
public class FallbackHandler {
    public Mono<ServerResponse> all(ServerRequest req) {
        System.out.println("request header size: " + req.headers().header("Test-Header").size());
        System.out.println("request header size: " + req.headers().header("Execution-Exception-Message").size());

        return ok().body(Mono.just("fallback"), String.class);
    }
}

