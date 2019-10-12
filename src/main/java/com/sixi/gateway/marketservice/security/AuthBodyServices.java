package com.sixi.gateway.marketservice.security;

import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthProblemException;
import com.sixi.gateway.checksigncommon.oauth.json.SingleJSON;
import com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthValidator;
import com.sixi.gateway.marketservice.constant.AuthConast;
import com.sixi.gateway.marketservice.exception.ErrorCode;
import com.sixi.gateway.marketservice.exception.ServerException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Author: ZY
 * @Date: 2019/8/22 17:07
 * @Version 1.0
 * @Description:
 */
public class AuthBodyServices {

    /**
     * 获取POST内容并转换为AuthMessage
     *
     * @param request
     * @return
     */
    public AuthMessage getAuthBody(ServerHttpRequest request) {
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
    private AuthMessage readMessage(String str) {

        Object obj = SingleJSON.paser(str);

        if (obj instanceof Map) {
            AuthMessage authMessage = new AuthMessage(((Map<String, ?>) obj).entrySet());
            try {
                //是否有必要参数
                authMessage.requireParameters(SimpleAuthValidator.SINGLE_PARAMETERS);
                return authMessage;
            } catch (AuthProblemException e) {
                ErrorCode errorCode = new ErrorCode(AuthConast.RESP_CD_MISSING_SIGNATURE_PARAM, AuthConast.RESP_MSG_MISSING_SIGNATURE_PARAM, "pls check your params!");
                throw new ServerException(HttpStatus.BAD_REQUEST, errorCode);
            }
        } else {
            ErrorCode errorCode = new ErrorCode(AuthConast.RESP_CD_MISSING_SIGNATURE_PARAM, AuthConast.RESP_MSG_MISSING_SIGNATURE_PARAM, "pls check your params!");
            throw new ServerException(HttpStatus.BAD_REQUEST, errorCode);
        }
    }

}
