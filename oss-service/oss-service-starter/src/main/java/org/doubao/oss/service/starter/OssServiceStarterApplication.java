package org.doubao.oss.service.starter;

import org.doubao.mall.common.config.UserContextAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = UserContextAutoConfiguration.class)
@EnableDiscoveryClient
public class OssServiceStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssServiceStarterApplication.class, args);
    }

}