package org.doubao.oss.service;

import org.doubao.mall.common.config.UserContextAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = UserContextAutoConfiguration.class)
@EnableDiscoveryClient
public class OssServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssServiceApplication.class, args);
    }

}