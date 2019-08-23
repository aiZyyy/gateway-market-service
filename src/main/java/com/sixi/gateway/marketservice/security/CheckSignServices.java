package com.sixi.gateway.marketservice.security;

import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.domain.AuthConsumer;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthException;
import com.sixi.gateway.checksigncommon.oauth.exception.AuthProblemException;
import com.sixi.gateway.checksigncommon.oauth.method.impl.RedisAuthNonces;
import com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import static com.sixi.gateway.checksigncommon.oauth.method.impl.SimpleAuthNonces.DEFAULT_MAX_TIMESTAMP_AGE;

/**
 * @Author: ZY
 * @Date: 2019/8/22 14:18
 * @Version 1.0
 * @Description: 验签功能
 */
public class CheckSignServices {

    static final String KEY = "MARKET:";

    static final String OAUTH_APP_ID_NAME = "app_id";

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
            return (ServerHttpRequest) e;
        } catch (AuthException e) {
            logger.error("Failed to CheckSign", e);
            return (ServerHttpRequest) e;
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
        //是否有必要参数
        authMessage.requireParameters(SimpleAuthValidator.SINGLE_PARAMETERS);
        //封装AuthConsumer
        AuthConsumer authConsumer = AuthConsumer.builder().key(appId).secret(publicKey).build();
        //封装redis防重类
        RedisAuthNonces redisAuthNonces = new RedisAuthNonces();
        //封装验证类
        SimpleAuthValidator simpleAuthValidator = new SimpleAuthValidator(redisAuthNonces, DEFAULT_MAX_TIMESTAMP_AGE);
        //验证签名
        simpleAuthValidator.validateMessage(authMessage, authConsumer, stringRedisTemplate);
    }


}
