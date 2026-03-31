package org.doubao.interview.agent.server.service.designpattern;

import java.sql.ResultSet;

/**
 * 行映射器接口（回调接口）
 * 
 * 模拟 Spring 的 RowMapper
 * 用于将结果集的每一行映射为 Java 对象
 * 
 * 这是模板方法模式中的"可变步骤"
 * 用户通过实现此接口自定义结果集处理逻辑
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public interface RowMapper<T> {
    
    /**
     * 将结果集的一行映射为对象
     * 
     * @param rs 结果集
     * @param rowNum 行号（从 1 开始）
     * @return 映射后的对象
     * @throws Exception 映射异常
     */
    T mapRow(ResultSet rs, int rowNum) throws Exception;
}
