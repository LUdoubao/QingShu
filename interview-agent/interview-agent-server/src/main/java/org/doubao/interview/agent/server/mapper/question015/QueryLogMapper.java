package org.doubao.interview.agent.server.mapper.question015;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.interview.agent.server.entity.q015.QueryLog;

/**
 * 查询日志 Mapper 接口 - 用于演示 Next-Key Lock 和幻读
 * 
 * 对应面试知识点：问题 015 - Next-Key Lock 与幻读
 * 
 * 【类注释】
 * 职责：提供查询日志表的数据库访问接口
 * 边界：仅用于演示，不涉及复杂查询和性能优化
 * 线程安全：MyBatis Plus 的 Mapper 是线程安全的
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Mapper
public interface QueryLogMapper extends BaseMapper<QueryLog> {
    // 继承 BaseMapper 后，无需编写 XML，即可使用 CRUD 方法
}
