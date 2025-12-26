package org.doubao.dialog.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.doubao.dialog.service.mapper")
@EnableFeignClients(basePackages = "org.doubao.dialog.service.feign")
@ComponentScan(basePackages = "org.doubao.dialog.service")
public class DialogServiceStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DialogServiceStarterApplication.class, args);
    }

}