package org.doubao.interview.agent.server.mapper.question012;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.interview.agent.server.entity.q012.Account;

/**
 * 账户 Mapper 接口 - 用于演示三种日志
 * 
 * 对应面试知识点：问题 012 - undo log、redo log、binlog 的区别
 * 
 * 【类注释】
 * 职责：提供账户表的数据库访问接口
 * 边界：仅用于演示，不涉及复杂查询和性能优化
 * 线程安全：MyBatis Plus 的 Mapper 是线程安全的
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
    // 继承 BaseMapper 后，无需编写 XML，即可使用 CRUD 方法
    // selectById, insert, updateById, delete 等方法均可直接使用
}
