package org.doubao.interview.agent.server.service.designpattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模拟处理器适配器
 */
public class MockHandlerAdapter implements HandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(MockHandlerAdapter.class);
    
    @Override
    public Object handle(Object handler, String request) throws Exception {
        log.info("执行处理器：{}", handler.getClass().getSimpleName());
        return "Mock Model Data for " + request;
    }
}
