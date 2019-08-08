package com.sixi.gateway.marketservice.filter;

import com.sixi.core.marketservice.api.AppApplyServiceApi;
import com.sixi.core.marketservice.domain.form.AppIdForm;
import com.sixi.gateway.checksigncommon.constant.AuthCodeConast;
import com.sixi.gateway.checksigncommon.constant.FailedResponse;
import com.sixi.gateway.checksigncommon.oauth.Auth;
import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.domain.AuthConsumer;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthException;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthProblemException;
import com.sixi.gateway.checksigncommon.oauth.json.SingleJSON;
import com.sixi.gateway.checksigncommon.oauth.method.AuthValidator;
import com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: ZY
 * @Date: 2019/8/5 13:33
 * @Version 1.0
 * @Description: 全局过滤器
 */

@Service
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    static final String OAUTH_APP_ID_NAME = "app_id";

    static final String OAUTH_SIGN_METHOD_NAME = "sign_type";

    static final String OAUTH_SIGN_NAME = "sign";

    static final String REQUEST_TYPE = "post";


    @Resource
    AppApplyServiceApi appApplyServiceApi;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        FailedResponse failed;
        System.out.println("Welcome to AuthGlobalFilter.");
        try {
            //将请求体中信息转换为AuthMessage
            AuthMessage message = resolveBodyFromRequest(serverHttpRequest);
            message.requireParameters(SimpleAuthValidator.SINGLE_PARAMETERS);
            String appId = message.getParameter(OAUTH_APP_ID_NAME);
            //获取应用公钥
            String appPublicKey = appApplyServiceApi.selectPublicKey(AppIdForm.builder().appId(appId).build()).getAppPublicKey();
            //封装验证类
            AuthConsumer authConsumer = AuthConsumer.builder().key(appId).secret(appPublicKey).build();
            //构造验证类
            SimpleAuthValidator simpleAuthValidator = new SimpleAuthValidator(30 * 60 * 100);
            //验证签名
            simpleAuthValidator.validateMessage(message, authConsumer);
            System.out.println("验签成功");
        } catch (AuthProblemException e) {
            //授权失败，不对其进行路由
            failed = getAuthFailedResponse(e);
        } catch (AuthException e) {
            failed = FailedResponse.GATEWAY_UNKNOWN_ERROR;
        }

//            //下面的将请求体再次封装写回到request里，传到下一级，否则，由于请求体已被消费，后续的服务将取不到值
//            URI uri = serverHttpRequest.getURI();
//            ServerHttpRequest request = serverHttpRequest.mutate().uri(uri).build();
//            DataBuffer bodyDataBuffer = stringBuffer(bodyStr);
//            Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

//            request = new ServerHttpRequestDecorator(request) {
//                @Override
//                public Flux<DataBuffer> getBody() {
//                    return bodyFlux;
//                }
//            };
        //封装request，传给下一级
//            return chain.filter(exchange.mutate().request(request).build());

        return chain.filter(exchange);
    }

//    private DataBuffer stringBuffer(String value) {
//        if (StringUtils.isNotEmpty(value)) {
//            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
//            NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
//            DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
//            buffer.write(bytes);
//            return buffer;
//        }
//        return null;
//    }

    /**
     * 扫描请求数据, 按json 一级结构解析成authMessage
     *
     * @return 请求体
     */
    private AuthMessage resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        //获取request body
        Object obj = SingleJSON.paser(bodyRef.get());
        if (obj instanceof Map) {
            return new AuthMessage(((Map<String, ?>) obj).entrySet());
        } else {
            AuthProblemException problem = new AuthProblemException(Auth.Problems.PARAMETER_ABSENT);
            problem.setParameter(Auth.Problems.OAUTH_PARAMETERS_ABSENT, Auth.OAUTH_SIGNATURE);
            throw problem;
        }
    }


    /**
     * AuthProblemException 转化成FailedResponse
     * 当缺少参数时，将具体的参数拼装成message,其他从全响的失败报文集合中获取。
     *
     * @param exception 异常
     * @return 响应
     */
    private FailedResponse getAuthFailedResponse(AuthProblemException exception) {
        String problem = exception.getProblem();
        FailedResponse failedResponse = getAuthFailedCodeMap().get(problem);
        if (failedResponse == null) {
            if (Auth.Problems.PARAMETER_ABSENT.equals(problem)) {
                String missParam = (String) exception.getParameters().get(Auth.Problems.OAUTH_PARAMETERS_ABSENT);
                String message = String.format(AuthCodeConast.RESP_MSG_MISSING_SIGNATURE_PARAM, missParam);
                return new FailedResponse(AuthCodeConast.RESP_CD_MISSING_SIGNATURE_PARAM, message);
            } else {
                return FailedResponse.UNKNOWN_ERROR;
            }
        }
        return failedResponse;
    }

    HashMap<String, FailedResponse> authFailedCodeMap = null;

    /**
     * 授权的失败的响应集合
     *
     * @return
     */
    private Map<String, FailedResponse> getAuthFailedCodeMap() {

        if (authFailedCodeMap == null) {
            HashMap map = new HashMap<String, FailedResponse>();

            map.put(Auth.Problems.NONCE_USED,
                    new FailedResponse(AuthCodeConast.RESP_CD_NONCE_USED, AuthCodeConast.RESP_MSG_NONCE_USED));

            map.put(Auth.Problems.SIGNATURE_INVALID,
                    new FailedResponse(AuthCodeConast.RESP_CD_INVALID_SIGNATURE, AuthCodeConast.RESP_MSG_INVALID_SIGNATURE));

            map.put(Auth.Problems.SIGNATURE_METHOD_REJECTED,
                    new FailedResponse(AuthCodeConast.RESP_CD_INVALID_SIGNATURE_TYPE, AuthCodeConast.RESP_MSG_INVALID_SIGNATURE_TYPE));

            map.put(Auth.Problems.CONSUMER_KEY_REFUSED,
                    new FailedResponse(AuthCodeConast.RESP_CD_REFUSED_APP_ID, AuthCodeConast.RESP_MSG_REFUSED_APP_ID));

            map.put(Auth.Problems.CONSUMER_KEY_REJECTED,
                    new FailedResponse(AuthCodeConast.RESP_CD_REFUSED_APP_ID, AuthCodeConast.RESP_MSG_REFUSED_APP_ID));

            map.put(Auth.Problems.CONSUMER_KEY_UNKNOWN,
                    new FailedResponse(AuthCodeConast.RESP_CD_INVALID_APP_ID, AuthCodeConast.RESP_MSG_INVALID_APP_ID));

            map.put(Auth.Problems.TIMESTAMP_REFUSED,
                    new FailedResponse(AuthCodeConast.RESP_CD_INVALID_TIMESTAMP, AuthCodeConast.RESP_MSG_INVALID_TIMESTAMP));


        }
        return authFailedCodeMap;
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
