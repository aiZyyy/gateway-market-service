package com.sixi.gateway.marketservice.filter;

import com.sixi.gateway.checksigncommon.oauth.Auth;
import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.domain.AuthConsumer;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthException;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthProblemException;
import com.sixi.gateway.checksigncommon.oauth.json.SingleJSON;
import com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthNonces;
import com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthValidator;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Author: ZY
 * @Date: 2019/8/9 15:59
 * @Version 1.0
 * @Description:
 */
@Component
@Slf4j
public class RequestFilter implements GatewayFilter, Ordered {

    static final String KEY = "MARKET:";

    static final String OAUTH_APP_ID_NAME = "app_id";

    static final String OAUTH_SIGN = "sign";

//    static final String STORE_ID = "SignCheck:";

    @Autowired
    RedisTemplate<String, String> redisTemplate;

//    private RedisDistributedKit redisDistributedKit;
//
//    public RequestFilter(RedisDistributedKit redisDistributedKit) {
//        this.redisDistributedKit = redisDistributedKit;
//    }

    Logger logger = LoggerFactory.getLogger(RequestFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest exchangeRequest = exchange.getRequest();
        String contentType = exchangeRequest.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        Flux<DataBuffer> body = exchangeRequest.getBody();
        // 缓存读取的request body信息
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(dataBuffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
            DataBufferUtils.release(dataBuffer);
            bodyRef.set(charBuffer.toString());
        });
        // 获取body信息
        String bodyStr = bodyRef.get();
        // 这种处理方式，必须保证post请求时，原始post表单必须有数据过来，不然会报错
        if (StringUtils.isBlank(bodyStr)) {
            logger.error("请求异常：{} POST请求必须传递参数", exchangeRequest.getURI().getRawPath());
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.setComplete();
        }
        //转换数据
        String updateStr = Arrays.stream(org.springframework.util.StringUtils.tokenizeToStringArray(bodyStr, "\n\t [ ]")).collect(Collectors.joining(""));
        AuthMessage authMessage;
        try {
            //将信息转换为authMessage对象
            authMessage = readMessage(updateStr);
            checkSign(authMessage);
            logger.info("验签成功,进入下一步转发");
        } catch (AuthProblemException e) {
            log.error("验签失败");
            //授权失败，不对其进行路由
            return Mono.error(e);
        } catch (IOException e) {
            return Mono.error(e);
        } catch (AuthException e) {
            return Mono.error(e);
        } catch (InterruptedException e) {
            return Mono.error(e);
        }
        ServerHttpRequest request = transferRequest(exchangeRequest, contentType, bodyStr, authMessage);
        return chain.filter(exchange.mutate().request(request).build());

    }

    /**
     * 转发新请求
     *
     * @param exchangeRequest
     * @param contentType
     * @param bodyStr
     * @param authMessage
     * @return
     */
    private ServerHttpRequest transferRequest(ServerHttpRequest exchangeRequest, String contentType, String bodyStr, AuthMessage authMessage) {
        //获取请求参数
        String biz_content = Arrays.stream(org.springframework.util.StringUtils.tokenizeToStringArray(authMessage.getParameter("biz_content"), "\n\t")).collect(Collectors.joining(""));
        //获取方法路径
        String newPath = "/" + Arrays.stream(org.springframework.util.StringUtils.tokenizeToStringArray(authMessage.getParameter("method"), "\\.")).collect(Collectors.joining("/"));
        //下面的将请求体再次封装写回到request里，传到下一级，否则，由于请求体已被消费，后续的服务将取不到值
        ServerHttpRequest request = exchangeRequest.mutate().path(newPath).build();
        DataBuffer bodyDataBuffer = stringBuffer(biz_content);
        Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

        // 定义新的消息头
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchangeRequest.getHeaders());

        // 由于修改了传递参数，需要重新设置CONTENT_LENGTH，长度是字节长度，不是字符串长度
        int length = biz_content.getBytes().length;
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        headers.setContentLength(length);

        // 设置CONTENT_TYPE
        if (StringUtils.isNotBlank(contentType)) {
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        }

        //封装新的request
        request = new ServerHttpRequestDecorator(request) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }
                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return bodyFlux;
            }
        };
        request.mutate().header(HttpHeaders.CONTENT_LENGTH, Integer.toString(bodyStr.length()));
        return request;
    }

    /**
     * 签名验证
     *
     * @param authMessage
     * @throws InterruptedException
     */
    private void checkSign(AuthMessage authMessage) throws InterruptedException {
        //获取签名
        String sign = authMessage.getParameter(OAUTH_SIGN);
        //获取appId
        String appId = authMessage.getParameter(OAUTH_APP_ID_NAME);
        //获取应用公钥
        String publicKey = redisTemplate.opsForValue().get(KEY + appId);
        //获取分布式锁
//        String token = redisDistributedKit.acquire(sign, STORE_ID, 5000, 4000);
        //是否有必要参数
        authMessage.requireParameters(SimpleAuthValidator.SINGLE_PARAMETERS);
        //封装验证类
        AuthConsumer authConsumer = AuthConsumer.builder().key(appId).secret(publicKey).build();
        SimpleAuthNonces simpleAuthNonces = new SimpleAuthNonces(60000L);
        SimpleAuthValidator simpleAuthValidator = new SimpleAuthValidator(simpleAuthNonces,1*60*1000);
        //验证签名
        simpleAuthValidator.validateMessage(authMessage, authConsumer);
//        redisDistributedKit.release(sign, STORE_ID, token);
    }


    /**
     * 转换消息格式
     *
     * @param value
     * @return
     */
    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }

    /**
     * 扫描请求数据, 按json 一级结构解析成authMessage
     *
     * @return AuthMessage对象
     * @throws AuthProblemException 解包错误
     * @throws IOException          请求数据读取错误
     */
    private AuthMessage readMessage(String str) throws AuthProblemException, IOException {

        Object paser = SingleJSON.paser(str);

        if (paser instanceof Map) {
            return new AuthMessage(((Map<String, ?>) paser).entrySet());
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
