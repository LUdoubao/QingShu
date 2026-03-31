package org.doubao.interview.agent.server.service.designpattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模拟视图解析器
 */
public class MockViewResolver implements ViewResolver {
    private static final Logger log = LoggerFactory.getLogger(MockViewResolver.class);
    
    @Override
    public String resolveView(Object modelAndView) {
        log.debug("解析视图：{}", modelAndView);
        return "success";
    }
}
