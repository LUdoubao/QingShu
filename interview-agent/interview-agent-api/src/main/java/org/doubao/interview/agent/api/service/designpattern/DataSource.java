package org.doubao.interview.agent.api.service.designpattern;

/**
 * 数据源接口
 * 
 * 模拟 Java JDBC 的 DataSource 接口
 * 用于演示装饰器模式在 Spring 事务管理中的应用
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface DataSource {
    
    /**
     * 获取数据库连接
     * 
     * @return 数据库连接对象
     */
    Object getConnection();
    
    /**
     * 数据源名称
     * 
     * @return 名称
     */
    String getName();
}
