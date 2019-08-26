package com.sixi.gateway.marketservice;

import com.sixi.gateway.checksigncommon.oauth.AuthMessage;
import com.sixi.gateway.checksigncommon.oauth.SignerBuilder;
import com.sixi.gateway.checksigncommon.oauth.domain.AuthConsumer;
import com.sixi.gateway.checksigncommon.oauth.method.ISignatureMethod;

/**
 * @Author: ZY
 * @Date: 2019/8/19 11:11
 * @Version 1.0
 * @Description:
 */
public class test {
    public static void main(String[] args) {
        AuthConsumer authConsumer = AuthConsumer.builder().key("app86715119635140608")
        .secret(
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDUWEjtfFhntXM2GnIx/su+vQUPYrz1r/ETqQNxAkgMSGXlA6kxrS/DUUtyXz2vfPVH97K9HoQZiMgdW++l948fIu2nXYRs3cyk+UjW23hJNbTqvqhPMFK6BaebVT32EilQFb9xYpRH784hu0qv8CL1OfCQwqErZHAIcMVpF3AsUwIDAQAB"
        ).build();
        AuthMessage authMessage = new AuthMessage();
        authMessage.addParameter("sequence", "1234567890");
        authMessage.addParameter("app_id", "app86715119635140608");
        authMessage.addParameter("biz_content", "{'userId':'18668066355'}");
        long timeMillis = System.currentTimeMillis();
        System.out.println(timeMillis);
        authMessage.addParameter("timestamp", String.valueOf(timeMillis));
        authMessage.addParameter("sign_type", "MD5");
        authMessage.addParameter("charset", "utf-8");
        authMessage.addParameter("method", "staff.test.helloworld");
        ISignatureMethod rsa = SignerBuilder.newSigner(authMessage);
        String signature = rsa.getSignature(authMessage, authConsumer);
        System.out.println(signature);
    }
}
