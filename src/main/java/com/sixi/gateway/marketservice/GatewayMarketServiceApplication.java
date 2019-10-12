package com.sixi.gateway.marketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA
 *
 * @author MiaoWoo
 */
@SpringCloudApplication
@RestController
public class GatewayMarketServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayMarketServiceApplication.class, args);
    }

}
