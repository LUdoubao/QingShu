package org.doubao.interview.agent.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Interview Agent 启动类。
 */
@SpringBootApplication(excludeName = "org.doubao.mall.common.config.UserContextAutoConfiguration")
@ComponentScan(basePackages = "org.doubao.interview.agent")
public class InterviewAgentStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewAgentStarterApplication.class, args);
    }
}
