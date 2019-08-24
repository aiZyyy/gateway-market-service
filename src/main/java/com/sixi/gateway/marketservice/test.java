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
        "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANRYSO18WGe1czYacjH+y769BQ9ivPWv8ROpA3ECSAxIZeUDqTGtL8NRS3JfPa989Uf3sr0ehBmIyB1b76X3jx8i7addhGzdzKT5SNbbeEk1tOq+qE8wUroFp5tVPfYSKVAVv3FilEfvziG7Sq/wIvU58JDCoStkcAhwxWkXcCxTAgMBAAECgYAsmlbHO6U+M4j9xlRSWBjn+cBEWRMj8E8NOCU26anEmrh8gGZbvusXdc4JOiQ05mSHN2pd+zj4PBj8wcD29ackGzkIenk99D8odIPaTHl69UtZCUsZR0Sf1qCwNInRIkS/XFg9PDl+ThZIr+9bVrJBPBojstJA/hpcansFMnmZAQJBAOobYSh1HfN9GKxCXqQuZa14QOMYK4GO2V50a8cljJEYute5TAnfjQuKcgpxr+zAKXosnNxhjl7RQ9AQlhYv3sECQQDoM+lh63u+EUYWH1+zG8eo/5YYoa8TVklBBU+qxVQw4PIOUA5tTCIcOqJMFzzSq6iOyzWG/3VxHsbrB1MR76QTAkEAqK/X32YckdGHEMC2H0mLXGa1Iq3M04sSF9x9uL7Wvcp6/2x2XPrnT/SPXfUzVb3VZUJ2ONpW0v2pBmidLRmLQQJALzGd1ZEO7WqFXCm0Qar7wZVw5EC4XK3E5bo2nvbMTcLqb45F4GEA7cvdAKoYapkJATy7/FwfeoZO16X9dD/IkQJAA5tZLSqas2SRGXwQwoKFO6gKPjOjEDcNpCiVym4dxetEOY8VSTqjPLKLtSXmoz6d7g72peSQmuKULub7H5tUDw=="
        ).build();
        AuthMessage authMessage = new AuthMessage();
        authMessage.addParameter("sequence", "1234567890");
        authMessage.addParameter("app_id", "app86715119635140608");
        authMessage.addParameter("biz_content", "{'userId':'18668066355'}");
        long timeMillis = System.currentTimeMillis();
        System.out.println(timeMillis);
        authMessage.addParameter("timestamp", String.valueOf(timeMillis));
        authMessage.addParameter("sign_type", "RSA");
        authMessage.addParameter("charset", "utf-8");
        authMessage.addParameter("method", "staff.user.select");
        ISignatureMethod rsa = SignerBuilder.newSigner(authMessage);
        String signature = rsa.getSignature(authMessage, authConsumer);
        System.out.println(signature);
    }
}
