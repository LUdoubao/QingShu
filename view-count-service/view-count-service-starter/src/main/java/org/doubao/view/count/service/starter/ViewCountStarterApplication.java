package org.doubao.view.count.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("org.doubao.view.count.service.mapper")
@EnableScheduling
@EnableFeignClients(basePackages = "org.doubao.view.count.service.feign")
public class ViewCountStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ViewCountStarterApplication.class, args);
    }

}