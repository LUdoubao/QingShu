package org.doubao.interview.agent.api.service.designpattern;

/**
 * AOP 通知接口
 * 
 * 模拟 Spring AOP 的 Advice 接口
 * 定义横切逻辑的顶级接口
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface Advice {
    
    /**
     * 获取通知类型
     * 
     * @return 通知类型（如 before/after/around）
     */
    String getAdviceType();
}
