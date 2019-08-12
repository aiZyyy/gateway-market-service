package com.sixi.gateway.marketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA
 *
 * @author MiaoWoo
 */
@RestController
@SpringCloudApplication
@ComponentScan("com.sixi")
public class GatewayMarketServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayMarketServiceApplication.class, args);
    }

}
