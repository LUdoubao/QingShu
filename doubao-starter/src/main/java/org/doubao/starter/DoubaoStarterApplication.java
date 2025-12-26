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
        basePackages = {
                "org.doubao.ai.service",
                "org.doubao.auth.service",
                "org.doubao.comment.service", 
                "org.doubao.dialog.service",
                "org.doubao.fanout.service", 
                "org.doubao.favorite.service", 
                "org.doubao.feed.service", 
                "org.doubao.like.service", 
                "org.doubao.mall.common", 
                "org.doubao.notification.service",
                "org.doubao.oss.service", 
                "org.doubao.quote.service", 
                "org.doubao.search.service", 
                "org.doubao.share.service", 
                "org.doubao.user.service", 
                "org.doubao.view.count.service"
        },
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
        "org.doubao.*.mapper"
})
public class DoubaoStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoubaoStarterApplication.class, args);
    }

}