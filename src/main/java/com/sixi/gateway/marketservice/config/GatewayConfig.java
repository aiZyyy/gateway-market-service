package com.sixi.gateway.marketservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created with IDEA
 *
 * @Description: TODO
 * @Author:zhangchongfei
 * @Date:2019/8/16
 * @Time:11:10
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {
    public String url;
}
