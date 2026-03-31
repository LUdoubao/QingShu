package org.doubao.interview.agent.server.service.designpattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JdbcTemplate 模板类 - 模板方法模式
 * 
 * 模拟 Spring 的 JdbcTemplate
 * 职责：封装 JDBC 固定流程，将可变步骤暴露给用户
 * 
 * 设计要点：
 * 1. 固定流程：加载驱动、创建连接、执行 SQL、关闭资源
 * 2. 可变步骤：SQL 语句、结果集处理交给回调接口
 * 3. 模板方法用 final 修饰，防止子类修改流程
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
public class JdbcTemplate {
    
    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    
    /**
     * 数据源
     */
    private final javax.sql.DataSource dataSource;
    
    /**
     * 构造函数
     * 
     * @param dataSource 数据源
     */
    public JdbcTemplate(javax.sql.DataSource dataSource) {
        this.dataSource = dataSource;
        log.info("初始化 JdbcTemplate");
    }
    
    /**
     * 查询单个对象（模板方法）
     * 
     * 这是模板方法模式的核心：
     * 1. 固定流程在模板中实现
     * 2. 可变部分通过回调接口实现
     * 
     * @param sql SQL 语句
     * @param rowMapper 行映射回调接口
     * @param args SQL 参数
     * @return 查询结果
     */
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log.info("========== 开始执行查询 ==========");
        log.info("SQL: {}", sql);
        log.info("参数：{}", args != null ? args.length : 0);
        
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        
        try {
            // ========== 固定步骤 1：获取连接 ==========
            conn = dataSource.getConnection();
            log.info("[模板] 获取数据库连接成功");
            
            // ========== 固定步骤 2：创建 PreparedStatement ==========
            ps = conn.prepareStatement(sql);
            setParameters(ps, args);
            log.info("[模板] 创建 PreparedStatement 成功");
            
            // ========== 固定步骤 3：执行查询 ==========
            rs = ps.executeQuery();
            log.info("[模板] 执行 SQL 查询成功");
            
            // ========== 可变步骤：通过回调处理结果集 ==========
            T result = null;
            if (rs.next()) {
                // 调用用户实现的 RowMapper 接口
                result = rowMapper.mapRow(rs, rs.getRow());
                log.info("[回调] 结果集映射完成");
            }
            
            log.info("========== 查询执行完成 ==========");
            return result;
            
        } catch (Exception e) {
            log.error("查询执行失败", e);
            throw new RuntimeException("Query failed", e);
        } finally {
            // ========== 固定步骤 4：关闭资源 ==========
            closeResources(rs, ps, conn);
            log.info("[模板] 释放数据库资源");
        }
    }
    
    /**
     * 更新操作（INSERT/UPDATE/DELETE）
     * 
     * @param sql SQL 语句
     * @param args SQL 参数
     * @return 影响的行数
     */
    public int update(String sql, Object... args) {
        log.info("========== 开始执行更新 ==========");
        log.info("SQL: {}", sql);
        
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        
        try {
            // ========== 固定步骤 1：获取连接 ==========
            conn = dataSource.getConnection();
            log.info("[模板] 获取数据库连接成功");
            
            // ========== 固定步骤 2：创建 PreparedStatement ==========
            ps = conn.prepareStatement(sql);
            setParameters(ps, args);
            log.info("[模板] 创建 PreparedStatement 成功");
            
            // ========== 固定步骤 3：执行更新 ==========
            int rows = ps.executeUpdate();
            log.info("[模板] 执行更新成功，影响行数：{}", rows);
            
            log.info("========== 更新执行完成 ==========");
            return rows;
            
        } catch (Exception e) {
            log.error("更新执行失败", e);
            throw new RuntimeException("Update failed", e);
        } finally {
            // ========== 固定步骤 4：关闭资源 ==========
            closeResources(null, ps, conn);
            log.info("[模板] 释放数据库资源");
        }
    }
    
    /**
     * 设置 SQL 参数（辅助方法）
     */
    private void setParameters(java.sql.PreparedStatement ps, Object[] args) 
            throws Exception {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
        }
    }
    
    /**
     * 关闭数据库资源（固定流程）
     */
    private void closeResources(java.sql.ResultSet rs, 
                               java.sql.PreparedStatement ps, 
                               java.sql.Connection conn) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            log.warn("关闭资源时发生异常", e);
        }
    }
}
