package org.doubao.auth.service.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(excludeName  = "org.doubao.mall.common.config.UserContextAutoConfiguration")
@EnableDiscoveryClient
@ComponentScan(basePackages = "org.doubao.auth.service")
public class AuthServiceStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceStarterApplication.class, args);
    }

}