package org.doubao.interview.agent.api.dto.question012;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 转账响应 DTO
 * 
 * 对应面试知识点：问题 012 - 三种日志的区别
 * 
 * 【类注释】
 * 职责：返回转账操作的结果和三种日志的详细信息
 * 边界：仅展示日志信息，不涉及底层存储细节
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    /**
     * 交易是否成功
     */
    private Boolean success;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 消息提示
     */
    private String message;

    /**
     * 转出账户交易前余额
     */
    private BigDecimal fromBalanceBefore;

    /**
     * 转出账户交易后余额
     */
    private BigDecimal fromBalanceAfter;

    /**
     * 转入账户交易前余额
     */
    private BigDecimal toBalanceBefore;

    /**
     * 转入账户交易后余额
     */
    private BigDecimal toBalanceAfter;

    /**
     * undo log 信息 - 用于回滚
     */
    private UndoLogInfo undoLogInfo;

    /**
     * redo log 信息 - 用于崩溃恢复
     */
    private RedoLogInfo redoLogInfo;

    /**
     * binlog 信息 - 用于主从复制
     */
    private BinlogInfo binlogInfo;

    /**
     * undo log 信息类
     * 
     * 【类注释】
     * 职责：展示 undo log 的结构和作用
     * 
     * undo log 是逻辑日志，记录的是"反向操作"
     * 例如：INSERT -> DELETE，UPDATE(旧值) -> UPDATE(新值)，DELETE -> INSERT
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UndoLogInfo {
        /**
         * undo log 记录 ID
         */
        private String undoLogId;

        /**
         * 操作类型：INSERT/UPDATE/DELETE
         */
        private String operationType;

        /**
         * 回滚 SQL - 用于撤销当前操作
         * 示例：如果执行了 UPDATE account SET balance=100 WHERE id=1
         * 则回滚 SQL 为：UPDATE account SET balance=200 WHERE id=1（假设原值为 200）
         */
        private String rollbackSql;

        /**
         * 作用说明
         */
        private String description;

	}

    /**
     * redo log 信息类
     * 
     * 【类注释】
     * 职责：展示 redo log 的结构和作用
     * 
     * redo log 是物理日志，记录的是"数据页的物理修改"
     * 采用 WAL（Write-Ahead Logging）技术，循环写入
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedoLogInfo {
        /**
         * redo log 记录 ID
         */
        private String redoLogId;

        /**
         * 修改的数据页 ID
         */
        private String pageId;

        /**
         * 修改前的数据（十六进制表示）
         */
        private String beforeImage;

        /**
         * 修改后的数据（十六进制表示）
         */
        private String afterImage;

        /**
         * LSN（Log Sequence Number）- 日志序列号
         * 用于标识 redo log 的位置，崩溃恢复时按 LSN 顺序重放
         */
        private Long lsn;

        /**
         * 作用说明
         */
        private String description;
    }

    /**
     * binlog 信息类
     * 
     * 【类注释】
     * 职责：展示 binlog 的结构和作用
     * 
     * binlog 是逻辑日志，记录的是"SQL 语句的逻辑变更"
     * 追加写入，用于主从复制和数据恢复
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BinlogInfo {
        /**
         * binlog 记录 ID
         */
        private String binlogId;

        /**
         * binlog 文件名
         */
        private String binlogFileName;

        /**
         * binlog 中的位置偏移量
         */
        private Long position;

        /**
         * 执行的 SQL 语句（逻辑操作）
         */
        private String executedSql;

        /**
         * 事件类型：Insert/Update/Delete/Query
         */
        private String eventType;

        /**
         * 作用说明
         */
        private String description;
    }
}
