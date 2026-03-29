package org.doubao.interview.agent.server.mapper.question012;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.interview.agent.server.entity.q012.TransactionLog;

/**
 * 交易日志 Mapper 接口 - 记录转账操作的日志信息
 * 
 * 对应面试知识点：问题 012 - undo log、redo log、binlog 的区别
 * 
 * 【类注释】
 * 职责：提供交易日志表的数据库访问接口
 * 边界：仅用于演示和记录，不作为真实审计依据
 * 线程安全：MyBatis Plus 的 Mapper 是线程安全的
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Mapper
public interface TransactionLogMapper extends BaseMapper<TransactionLog> {
    // 继承 BaseMapper 后，无需编写 XML，即可使用 CRUD 方法
}
