package com.sixi.gateway.marketservice.services;

import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.domain.AuthConsumer;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthProblemException;
import com.sixi.gateway.checksigncommon.oauth.method.impl.RedisAuthNonces;
import com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthValidator;
import com.sixi.gateway.marketservice.constant.AuthConast;
import com.sixi.gateway.marketservice.exception.ErrorCode;
import com.sixi.gateway.marketservice.exception.ServerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;

import static com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthValidator.DEFAULT_TIMESTAMP_AGE;
import static com.sixi.gateway.marketservice.filter.AuthGatewayFilter.*;

/**
 * @Author: ZY
 * @Date: 2019/8/22 14:18
 * @Version 1.0
 * @Description: 验签功能
 */
public class CheckSignServices {

    static final String KEY = "MARKET:";

    static final String OAUTH_APP_ID_NAME = "app_id";

    static final String SKIP_ROUTES = "skip_routes";

    protected final Log logger = LogFactory.getLog(getClass());

    private StringRedisTemplate stringRedisTemplate;

    public CheckSignServices(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public ServerHttpRequest doCheckSign(ServerHttpRequest request, AuthMessage authMessage) {
        ServerHttpRequest req = request;
        try {
            checkSign(authMessage);
            logger.info("验签成功,进入下一步转发");
        } catch (AuthProblemException e) {
            //授权失败，不对其进行路由
            logger.error("Failed to CheckSign", e);
            ErrorCode errorCode = new ErrorCode(AuthConast.RESP_CD_INVALID_SIGNATURE, e.getProblem(), "pls check your timestamp or sign!");
            throw new ServerException(HttpStatus.BAD_REQUEST, errorCode);
        }
        return req;
    }


    /**
     * 签名验证
     *
     * @param authMessage
     */
    private void checkSign(AuthMessage authMessage) {
        //获取appId
        String appId = authMessage.getParameter(OAUTH_APP_ID_NAME);
        //获取应用公钥
        String publicKey = stringRedisTemplate.opsForValue().get(KEY + appId);
        //封装AuthConsumer
        AuthConsumer authConsumer = AuthConsumer.builder().key(appId).secret(publicKey).build();
        //封装redis防重类
        RedisAuthNonces redisAuthNonces = new RedisAuthNonces();
        //封装验证类
        SimpleAuthValidator simpleAuthValidator = new SimpleAuthValidator(redisAuthNonces, DEFAULT_TIMESTAMP_AGE);
        //验证签名
        simpleAuthValidator.validateMessage(authMessage, authConsumer, stringRedisTemplate);
    }

    /**
     * 调过验签
     *
     * @param methodValue
     * @param path
     * @param headers
     * @return
     */
    public boolean skipCheck(String methodValue, String path, HttpHeaders headers) {
        //不支持GET请求
        if (METHOD_VALUE.equals(methodValue)) {
            ErrorCode errorCode = new ErrorCode(AuthConast.RESP_CD_METHOS_TYPE, AuthConast.RESP_MSG_METHOS_TYPE, "暂不支持GET请求");
            throw new ServerException(HttpStatus.BAD_REQUEST, errorCode);
        }
        //如果为特定请求调过验签
        if (stringRedisTemplate.opsForHash().hasKey(SKIP_ROUTES, path)) {
            return true;
        }
        return false;
    }

}
