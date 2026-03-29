package org.doubao.interview.agent.api.dto.question015;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询响应 DTO - 展示 Next-Key Lock 的效果
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：返回查询结果和锁信息，对比演示幻读现象
 * 边界：仅展示锁的信息，不涉及底层实现细节
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {

    /**
     * 查询是否成功
     */
    private Boolean success;

    /**
     * 会话 ID
     */
    private String sessionId;

    /**
     * 查询类型
     */
    private String queryType;

    /**
     * 查询条件
     */
    private String queryCondition;

    /**
     * 查询结果数量
     */
    private Integer resultCount;

    /**
     * 查询结果数据
     */
    private List<AccountData> results;

    /**
     * 使用的锁信息
     */
    private LockInfo lockInfo;

    /**
     * 是否检测到幻读
     */
    private Boolean phantomDetected;

    /**
     * 账户数据类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountData {
        /**
         * 账户 ID
         */
        private Long id;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 账户余额
         */
        private String balance;

        /**
         * 账户状态
         */
        private Integer status;
    }

    /**
     * 锁信息类
     * 
     * 【类注释】
     * 职责：展示 Next-Key Lock 的类型和作用范围
     * 
     * Next-Key Lock = Record Lock + Gap Lock
     * - Record Lock: 锁住索引记录本身
     * - Gap Lock: 锁住索引记录之间的间隙
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockInfo {
        /**
         * 锁的类型
         * Next-Key Lock: 临键锁（Record Lock + Gap Lock）
         * Record Lock: 记录锁
         * Gap Lock: 间隙锁
         */
        private String lockType;

        /**
         * 锁的模式
         * X: 排他锁
         * S: 共享锁
         */
        private String lockMode;

        /**
         * 锁定的范围
         */
        private String lockRange;

        /**
         * 是否能防止幻读
         */
        private Boolean preventsPhantom;

        /**
         * 作用说明
         */
        private String description;
    }
}
