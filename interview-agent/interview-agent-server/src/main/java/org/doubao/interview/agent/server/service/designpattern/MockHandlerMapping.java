package org.doubao.interview.agent.server.service.designpattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模拟处理器映射器
 */
public class MockHandlerMapping implements HandlerMapping {
    private static final Logger log = LoggerFactory.getLogger(MockHandlerMapping.class);
    
    @Override
    public Object getHandler(String request) {
        log.debug("查找处理器：{}", request);
        // 简化实现，返回一个模拟处理器
        return new MockHandler();
    }
}
