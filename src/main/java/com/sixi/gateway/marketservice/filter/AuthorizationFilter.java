package com.sixi.gateway.marketservice.filter;

import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.marketservice.security.AuthBodyServices;
import com.sixi.gateway.marketservice.security.CheckSignServices;
import com.sixi.gateway.marketservice.security.EncapsulationServices;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

import static com.sixi.gateway.marketservice.security.EncapsulationServices.ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER;
import static com.sixi.gateway.marketservice.security.EncapsulationServices.SIXI_SERVICE;

/**
 * @Author: ZY
 * @Date: 2019/8/22 10:49
 * @Version 1.0
 * @Description: 全局过滤器
 */
public class AuthorizationFilter implements GlobalFilter, Ordered {


    private final CheckSignServices checkSignServices;

    private final EncapsulationServices encapsulationServices;

    private final AuthBodyServices authBodyServices;

    public AuthorizationFilter(CheckSignServices checkSignServices, AuthBodyServices authBodyServices, EncapsulationServices encapsulationServices) {
        this.checkSignServices = checkSignServices;
        this.authBodyServices = authBodyServices;
        this.encapsulationServices = encapsulationServices;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取request
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        //获取请求头信息
        HttpHeaders headers = serverHttpRequest.getHeaders();
        if ((null != headers.get(ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER).get(0)) && SIXI_SERVICE.equals(headers.get(ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER).get(0))) {
            return chain.filter(exchange);
        }

        //获取消息体信息
        AuthMessage authMessage = authBodyServices.getAuthBody(serverHttpRequest);
        //验签
        ServerHttpRequest req = checkSignServices.doCheckSign(serverHttpRequest, authMessage);
        //获取请求类型
        String contentType = serverHttpRequest.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        //封装新的请求
        ServerHttpRequest request = encapsulationServices.encapsulationRequest(req, contentType, authMessage);
        ServerHttpRequest build = request.mutate().header("@ignoreGlobalFilter", ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER).build();
        //封装新的exchange
        ServerWebExchange webExchange = exchange.mutate().request(build).build();
        //转发新需求
        return chain.filter(webExchange);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
