package org.doubao.interview.agent.api.service.question015;

import org.doubao.interview.agent.api.dto.question015.QueryRequest;
import org.doubao.interview.agent.api.dto.question015.QueryResponse;

/**
 * Next-Key Lock 演示服务接口
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：提供查询功能，展示 Next-Key Lock 如何避免幻读
 * 边界：仅用于演示和学习，不包含真实业务系统的复杂处理
 * 线程安全：接口本身不保证线程安全，实现类通过数据库锁保证并发安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
public interface NextKeyLockDemoService {

    /**
     * 执行查询操作 - 演示 Next-Key Lock 的效果
     * 
     * 【方法注释】
     * 输入约束：sessionId 不能为空；queryType 必须是 SNAPSHOT_READ 或 CURRENT_READ
     * 输出语义：返回查询结果和使用的锁信息
     * 异常场景：参数校验失败时抛出 BusinessException
     * 性能注意点：涉及数据库事务和锁操作，避免高频调用
     * 
     * @param request 查询请求参数
     * @return 查询响应，包含锁信息和是否检测到幻读
     */
    QueryResponse query(QueryRequest request);

    /**
     * 模拟插入操作 - 用于测试幻读
     * 
     * 【方法注释】
     * 输入约束：userName 和 balance 不能为空
     * 输出语义：返回插入结果
     * 异常场景：如果违反唯一约束则抛出异常
     * 性能注意点：在已有事务中执行，可能受到锁阻塞
     * 
     * @param sessionId 会话 ID（标识事务）
     * @param userName 用户名称
     * @param balance 初始余额
     * @return 插入结果描述
     */
    String simulateInsert(String sessionId, String userName, String balance);

    /**
     * 初始化演示数据
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：创建测试账户数据
     * 异常场景：如果表已存在数据则先清空
     * 
     * @return 初始化结果描述
     */
    String initDemoData();

    /**
     * 获取 Next-Key Lock 的详细说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回 Next-Key Lock 的原理和幻读抑制机制说明
     * 异常场景：无
     * 
     * @return Next-Key Lock 的详细说明（Markdown 格式）
     */
    String getNextKeyLockExplanation();
}
