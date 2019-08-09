package com.sixi.gateway.marketservice.filter;

/**
 * @Author: ZY
 * @Date: 2019/8/9 14:54
 * @Version 1.0
 * @Description:
 */

import com.sixi.gateway.checksigncommon.oauth.Auth;
import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.SignerBuilder;
import com.sixi.gateway.checksigncommon.oauth.domain.AuthConsumer;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthException;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthProblemException;
import com.sixi.gateway.checksigncommon.oauth.json.SingleJSON;
import com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthValidator;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * gateway全局过滤器
 * https://blog.csdn.net/forezp/article/details/85057268
 */
public class OauthFilter implements GlobalFilter, Ordered {

    static final String KEY = "MARKET:";

    static final String OAUTH_APP_ID_NAME = "app_id";

    static final String REQUEST_TYPE = "POST";

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    Logger logger = LoggerFactory.getLogger(OauthFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        URI ex = UriComponentsBuilder.fromUri(uri).build(true).toUri();
        ServerHttpRequest request = exchange.getRequest().mutate().uri(ex).build();
        // 判断是否为POST请求
        if (REQUEST_TYPE.equalsIgnoreCase(request.getMethodValue())) {
            Flux<DataBuffer> body = request.getBody();
            // 缓存读取的request body信息
            AtomicReference<String> bodyRef = new AtomicReference<>();
            body.subscribe(dataBuffer -> {
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
                DataBufferUtils.release(dataBuffer);
                bodyRef.set(charBuffer.toString());
            });
            //获取body信息
            String bodyStr = bodyRef.get();
            String updateStr = bodyStr.replaceAll("\n\t", "");
            System.out.println(bodyStr);
            AuthMessage authMessage = null;
            try {
                //将信息转换为authMessage对象
                authMessage = readMessage(updateStr);
                //是否有必要参数
//                authMessage.requireParameters(SimpleAuthValidator.SINGLE_PARAMETERS);

                AuthConsumer authConsumer = AuthConsumer.builder().key("app84508210140155904")
                        .secret("MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIuR34tq2mnIsIMfT83/xT8ljtIC3wHHFVIe/5DxamxGSJfkKgj5WFv3qXvTuoegQdjaAmmJEl/SwDPJdsGEqUvZwxBd+R0m4v1Gps3O8Er7a1pbuWsa/Yl+HCk2u2SMnqHvjoAYjgnsTE2MpMDZkG7LE7mSsjXbmEJUUGvTupDlAgMBAAECgYAzzagAcl+xJdlGQef4GPgYURNqpcAqQ7+JJJJNNR4AJDIrlnd3rzz5nbodiN/SGUx3dauxijv0rx/B2QQoHdpMDO00nyYS73/7ZXeMs/Wv1m6OSnLxssve0PY/SHDxbFJ8duZcHOpQnT4LxLR6wZyZ9msZ21YBBCWooe77J9EUnQJBANGT1PTiwXn15dXjG2rDApxl607gSKjJiQTYEtBfS/5aJ8HIqA+RNXEW+2BL+bpZe9FkgWL8vSh1OMlsbAek7kMCQQCqfDtFG8RCK4Wc9LdwAzAveA4kzHZArlazKJ3sMWlQtnIPhIqYcsd1AKslo+pYZizrzWvABy1jKB9tMg5P6FW3AkB8oGh255EeMXfnZRIcvrKCxqjTUtRiatYsJ0Go38KVEo+p0OT/vN4Gzh/V99gdVLEop5e5gYoK0Qpf3TWwpgd5AkB+dmTo4K32f54/TW/9EQBfVej39wsI88mwYEK0//olOxDk3eaJKys1aWeLJkohhLlxuRFignByi0K0l1ryf1+FAkB6xfpMonXZZH3IGTOduPay4TqY2YAUCPRlwYjP0wTu38S+NPx4Q7LxYWuvlASt9pxCG2ihhF0ukV9XynijEnt/").build();

                long timeMillis = System.currentTimeMillis();
                System.out.println(timeMillis / 1000L);
                authMessage.addParameter("timestamp", String.valueOf(timeMillis / 1000L));
                //获取签名
                String signature = SignerBuilder.newSigner(authMessage).getSignature(authMessage, authConsumer);

                //获取appId
                String appId = authMessage.getParameter(OAUTH_APP_ID_NAME);
                //获取应用公钥
                String publicKey = redisTemplate.opsForValue().get(KEY + appId);
                //封装验证类
                AuthConsumer authConsumer1 = AuthConsumer.builder().key(appId).secret(publicKey).build();
                //构造验证类
                SimpleAuthValidator simpleAuthValidator = new SimpleAuthValidator(30 * 60 * 100);
                authMessage.addParameter("sign", signature);
                //验证签名
                simpleAuthValidator.validateMessage(authMessage, authConsumer1);
                System.out.println("验签成功");
            } catch (AuthException e) {
                logger.error("验签失败");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //获取请求参数
            String biz_content = authMessage.getParameter("biz_content").replaceAll("/t", "");
            //获取方法路径
            String method = authMessage.getParameter("method");
            method = "localhost:8085/" + method.replaceAll("\\.", "/");
            URI url = null;
            try {
                url = new URI(method);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            //下面的将请求体再次封装写回到request里，传到下一级，否则，由于请求体已被消费，后续的服务将取不到值
            ServerHttpRequest request1 = request.mutate().uri(url).build();
            DataBuffer bodyDataBuffer = stringBuffer(biz_content);
            Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);
            //封装新的request
            request = new ServerHttpRequestDecorator(request1) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return bodyFlux;
                }
            };
        }
        //封装request，传给下一级
        return chain.filter(exchange.mutate().request(request).build());
    }


    protected DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }

    @Override
    public int getOrder() {
        return -999;
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

}

