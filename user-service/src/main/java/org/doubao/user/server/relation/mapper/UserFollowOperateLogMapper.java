package org.doubao.user.server.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.user.server.relation.entity.UserFollowOperateLog;

/**
 * 关注操作日志表Mapper
 */
@Mapper
public interface UserFollowOperateLogMapper extends BaseMapper<UserFollowOperateLog> {
}