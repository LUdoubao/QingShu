package org.doubao.interview.agent.server.entity.q015;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询日志实体类 - 记录幻读演示过程中的查询操作
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：记录查询操作的时间点和结果，用于对比演示幻读现象
 * 边界：仅用于演示，简化了真实审计日志的复杂性
 * 线程安全：非线程安全
 * 幂等性：通过日志 ID 唯一标识
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@TableName("query_log_015")
public class QueryLog {

    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话 ID - 标识同一个事务
     */
    private String sessionId;

    /**
     * 查询类型：SNAPSHOT_READ(快照读)/CURRENT_READ(当前读)
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
     * 是否发生幻读：0-否，1-是
     */
    private Boolean isPhantom;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
