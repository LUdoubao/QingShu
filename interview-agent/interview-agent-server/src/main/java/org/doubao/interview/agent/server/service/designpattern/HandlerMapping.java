package org.doubao.interview.agent.server.service.designpattern;

/**
 * 处理器映射器接口（委派模式的组件）
 */
public interface HandlerMapping {
    Object getHandler(String request);
}
