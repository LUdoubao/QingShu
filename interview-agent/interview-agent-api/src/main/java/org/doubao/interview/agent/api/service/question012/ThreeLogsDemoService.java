package org.doubao.interview.agent.api.service.question012;

import org.doubao.interview.agent.api.dto.question012.TransferRequest;
import org.doubao.interview.agent.api.dto.question012.TransferResponse;

/**
 * 转账服务接口 - 演示 undo log、redo log、binlog 的工作机制
 * 
 * 对应面试知识点：问题 012 - 三种日志的区别
 * 
 * 【类注释】
 * 职责：提供转账功能，展示 MySQL 事务中三种日志的协同工作过程
 * 边界：仅用于演示和学习，不包含真实金融系统的复杂处理
 * 线程安全：接口本身不保证线程安全，实现类通过事务和锁保证并发安全
 * 幂等性：通过 transactionNo 保证同一交易的幂等性
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
public interface ThreeLogsDemoService {

    /**
     * 执行转账操作 - 演示三种日志的完整流程
     * 
     * 【方法注释】
     * 输入约束：fromAccountId 和 toAccountId 必须存在且不为同一个账户；amount 必须大于 0.01
     * 输出语义：返回转账结果，包含三种日志的详细信息
     * 异常场景：
     *   - 余额不足时抛出 BusinessException
     *   - 账户不存在时抛出 BusinessException
     *   - simulateException=true 时模拟异常，演示 undo log 回滚
     * 性能注意点：涉及数据库事务操作，避免长事务持有连接
     * 
     * @param request 转账请求参数
     * @return 转账响应，包含三种日志的详细信息
     */
    TransferResponse transfer(TransferRequest request);

    /**
     * 查询账户信息
     * 
     * 【方法注释】
     * 输入约束：accountId 必须为正整数
     * 输出语义：返回账户的详细信息
     * 异常场景：账户不存在时返回 null
     * 
     * @param accountId 账户 ID
     * @return 账户信息（JSON 字符串格式，便于展示）
     */
    String getAccountInfo(Long accountId);

    /**
     * 初始化演示数据
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：创建两个测试账户，每个账户初始余额为 1000 元
     * 异常场景：如果表已存在数据则先清空
     * 
     * @return 初始化结果描述
     */
    String initDemoData();

    /**
     * 获取三种日志的详细对比说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回三种日志的对比表格和详细说明
     * 异常场景：无
     * 
     * @return 三种日志的对比说明（Markdown 格式）
     */
    String getLogsComparison();
}
