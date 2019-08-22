package com.sixi.gateway.marketservice.filter;

import com.sixi.gateway.checksigncommon.oauth.Auth;
import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthProblemException;
import com.sixi.gateway.checksigncommon.oauth.json.SingleJSON;
import com.sixi.gateway.marketservice.security.CheckSignServices;
import com.sixi.gateway.marketservice.security.EncapsulationServices;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Author: ZY
 * @Date: 2019/8/22 10:49
 * @Version 1.0
 * @Description: 全局过滤器
 */
public class AuthorizationFilter implements GlobalFilter, Ordered {

    private final CheckSignServices checkSignServices;

    private final EncapsulationServices encapsulationServices;

    public AuthorizationFilter(CheckSignServices checkSignServices, EncapsulationServices encapsulationServices) {
        this.checkSignServices = checkSignServices;
        this.encapsulationServices = encapsulationServices;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        //获取消息体信息
        AuthMessage authMessage = getAuthBody(serverHttpRequest);
        //验签
        ServerHttpRequest req = checkSignServices.doCheckSign(serverHttpRequest, authMessage);
        //获取请求类型
        String contentType = serverHttpRequest.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        //封装新的请求
        ServerHttpRequest request = encapsulationServices.encapsulationRequest(req, contentType, authMessage);
        //转发新需求
        return chain.filter(exchange.mutate().request(request).build());

    }

    /**
     * 获取POST内容并转换为AuthMessage
     *
     * @param request
     * @return
     */
    private AuthMessage getAuthBody(ServerHttpRequest request) {
        Flux<DataBuffer> body = request.getBody();
        // 缓存读取的request body信息
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(dataBuffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
            DataBufferUtils.release(dataBuffer);
            bodyRef.set(charBuffer.toString());
        });
        // 获取body信息
        String bodyStr = bodyRef.get();
        //转换数据
        String updateStr = Arrays.stream(org.springframework.util.StringUtils.tokenizeToStringArray(bodyStr, "\n\t [ ]")).collect(Collectors.joining(""));
        AuthMessage authMessage;
        //将信息转换为authMessage对象
        authMessage = readMessage(updateStr);
        return authMessage;
    }

    /**
     * 扫描请求数据, 按json 一级结构解析成authMessage
     *
     * @return AuthMessage对象
     * @throws AuthProblemException 解包错误
     */
    private AuthMessage readMessage(String str) throws AuthProblemException {

        Object obj = SingleJSON.paser(str);

        if (obj instanceof Map) {
            return new AuthMessage(((Map<String, ?>) obj).entrySet());
        } else {
            AuthProblemException problem = new AuthProblemException(Auth.Problems.PARAMETER_ABSENT);
            problem.setParameter(Auth.Problems.OAUTH_PARAMETERS_ABSENT, Auth.OAUTH_SIGNATURE);
            throw problem;
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
