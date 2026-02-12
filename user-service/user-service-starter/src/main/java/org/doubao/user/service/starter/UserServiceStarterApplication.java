package org.doubao.user.service.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "org.doubao.user.service")
@EnableDiscoveryClient
@MapperScan({
        "org.doubao.user.service.mapper.core",
        "org.doubao.user.service.mapper.relation",
        "org.doubao.user.service.mapper.report",
        "org.doubao.user.service.mapper.log"
})
@EnableMongoRepositories("org.doubao.user.service.repository")
@EnableFeignClients({
        "org.doubao.user.service.feign.core",
        "org.doubao.user.service.feign.report"
})
@EnableAspectJAutoProxy
public class UserServiceStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceStarterApplication.class, args);
    }

}