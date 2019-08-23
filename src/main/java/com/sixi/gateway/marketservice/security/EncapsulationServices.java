package com.sixi.gateway.marketservice.security;

import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import io.netty.buffer.ByteBufAllocator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author: ZY
 * @Date: 2019/8/22 15:01
 * @Version 1.0
 * @Description: 封装新消息
 */
public class EncapsulationServices {



    /**
     * 转发新请求
     *
     * @param exchangeRequest
     * @param contentType
     * @param authMessage
     * @return
     */
    public ServerHttpRequest encapsulationRequest(ServerHttpRequest exchangeRequest, String contentType, AuthMessage authMessage) {
        //获取请求参数
        String biz_content = Arrays.stream(org.springframework.util.StringUtils.tokenizeToStringArray(authMessage.getParameter("biz_content"), "\n\t")).collect(Collectors.joining(""));
        if (StringUtils.isEmpty(biz_content)) {
            biz_content = " ";
        }
        //获取方法路径
        String newPath = "/" + Arrays.stream(org.springframework.util.StringUtils.tokenizeToStringArray(authMessage.getParameter("method"), "\\.")).collect(Collectors.joining("/"));
        //下面的将请求体再次封装写回到request里，传到下一级，否则，由于请求体已被消费，后续的服务将取不到值
        ServerHttpRequest request = exchangeRequest.mutate().path(newPath).build();
        DataBuffer bodyDataBuffer = stringBuffer(biz_content);
        Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

        // 定义新的消息头
        HttpHeaders headers = new HttpHeaders();
//        //添加过滤条件
//        ArrayList<String> arr = new ArrayList<>();
//        arr.add(SIXI_SERVICE);
//        headers.put(ATTRIBUTE_IGNORE_TEST_GLOBAL_FILTER,arr);
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
        request.mutate().header(HttpHeaders.CONTENT_LENGTH, Integer.toString(length));
        return request;
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
}
