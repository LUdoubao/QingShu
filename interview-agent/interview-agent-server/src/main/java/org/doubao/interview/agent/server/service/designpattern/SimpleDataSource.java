package org.doubao.interview.agent.server.service.designpattern;

import org.doubao.interview.agent.api.service.designpattern.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基础数据源实现（被装饰的原始对象）
 * 
 * 模拟 Spring 中的 DriverManagerDataSource
 * 提供基础的数据库连接功能，不包含事务等增强功能
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class SimpleDataSource implements DataSource {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleDataSource.class);
    
    /**
     * 数据源名称
     */
    private final String name;
    
    /**
     * JDBC URL
     */
    private final String jdbcUrl;
    
    /**
     * 构造函数
     * 
     * @param name 数据源名称
     * @param jdbcUrl JDBC 连接 URL
     */
    public SimpleDataSource(String name, String jdbcUrl) {
        this.name = name;
        this.jdbcUrl = jdbcUrl;
        log.info("创建基础数据源：{}, url={}", name, jdbcUrl);
    }
    
    /**
     * 获取数据库连接
     * 
     * @return 数据库连接对象
     */
    @Override
    public Object getConnection() {
        log.info("[基础数据源] 获取数据库连接：{}", jdbcUrl);
        // 模拟返回一个连接对象
        return new MockConnection(jdbcUrl);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * 模拟数据库连接类
     */
    private static class MockConnection {
        private final String url;
        
        public MockConnection(String url) {
            this.url = url;
            log.debug("创建数据库连接：{}", url);
        }
        
        public String getUrl() {
            return url;
        }
    }
}
