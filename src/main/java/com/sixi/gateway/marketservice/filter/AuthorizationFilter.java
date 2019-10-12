package com.sixi.gateway.marketservice.filter;

import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.marketservice.constant.AuthConast;
import com.sixi.gateway.marketservice.exception.ErrorCode;
import com.sixi.gateway.marketservice.exception.ServerException;
import com.sixi.gateway.marketservice.security.AuthBodyServices;
import com.sixi.gateway.marketservice.security.CheckSignServices;
import com.sixi.gateway.marketservice.security.EncapsulationServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Author: ZY
 * @Date: 2019/8/22 10:49
 * @Version 1.0
 * @Description: 全局过滤器
 */
@Slf4j
public class AuthorizationFilter implements GlobalFilter, Ordered {

    public final static String ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER = "ignoreGlobalFilter";

    public final static String SIXI_SERVICE = "sixiignoreservice";

    public final static String METHOD_VALUE = "GET";


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
        //获取请求方式
        String methodValue = serverHttpRequest.getMethodValue();
        //获取请求路径
        String path = serverHttpRequest.getPath().value();
        //获取请求头信息
        HttpHeaders headers = serverHttpRequest.getHeaders();
        if (checkSignServices.skipCheck(methodValue, path, headers)) {
            return chain.filter(exchange);
        }
        //获取消息体信息
        String body = authBodyServices.getAuthBody(serverHttpRequest);
        AuthMessage authMessage = authBodyServices.readMessage(body);
        //验签
        ServerHttpRequest req = checkSignServices.doCheckSign(serverHttpRequest, authMessage);
        //获取请求类型
        String contentType = serverHttpRequest.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        ServerWebExchange webExchange;
        try {
            //封装新的请求
            ServerHttpRequest request = encapsulationServices.encapsulationRequest(req, contentType, authMessage, body);
            ServerHttpRequest build = request.mutate().header(ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER, SIXI_SERVICE).build();
            //封装新的exchange
            webExchange = exchange.mutate().request(build).build();
            //转发新需求
            return chain.filter(webExchange);
        } catch (Exception e) {
            ErrorCode errorCode = new ErrorCode(AuthConast.RESP_CD_INVALID_PATH, AuthConast.RESP_MSG_INVALID_PATH, "pls check your path!");
            throw new ServerException(HttpStatus.BAD_REQUEST, errorCode);
        }
    }


    @Override
    public int getOrder() {
        return 1;
    }
}
