package org.doubao.interview.agent.server.service.designpattern;

/**
 * 处理器适配器接口（委派模式的组件）
 */
public interface HandlerAdapter {
    Object handle(Object handler, String request) throws Exception;
}
