package org.doubao.interview.agent.server.service.designpattern;

/**
 * 视图解析器接口（委派模式的组件）
 */
public interface ViewResolver {
    String resolveView(Object modelAndView);
}
