package org.doubao.interview.agent.api.dto.question015;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询请求 DTO - 用于演示 Next-Key Lock 和幻读
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：封装查询请求参数，演示快照读和当前读的区别
 * 边界：仅用于演示场景，简化了真实查询的复杂性
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
public class QueryRequest {

    /**
     * 会话 ID - 标识同一个事务
     * 相同 sessionId 的查询在同一个事务中执行
     */
    @NotNull(message = "会话 ID 不能为空")
    private String sessionId;

    /**
     * 查询类型
     * SNAPSHOT_READ: 快照读（普通 SELECT）
     * CURRENT_READ: 当前读（SELECT ... FOR UPDATE）
     */
    @NotNull(message = "查询类型不能为空")
    private String queryType;

    /**
     * 查询的最小 ID 范围
     */
    private Long minId;

    /**
     * 查询的最大 ID 范围
     */
    private Long maxId;

    /**
     * 是否启用 Next-Key Lock
     * true: 使用 FOR UPDATE（触发 Next-Key Lock）
     * false: 使用普通 SELECT（快照读）
     */
    private Boolean useNextKeyLock = false;

    /**
     * 备注说明
     */
    private String remark;
}
