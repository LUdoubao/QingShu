package org.doubao.starter;

import org.doubao.user.service.config.UserFeignErrorDecoderConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan(
        basePackages = {"org.doubao"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                org.doubao.comment.service.config.FeignErrorDecoderConfig.class,
                                UserFeignErrorDecoderConfig.class
                        }
                )
        }
)
@MapperScan({
        "org.doubao.**.mapper"
})
public class DoubaoStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoubaoStarterApplication.class, args);
    }

}