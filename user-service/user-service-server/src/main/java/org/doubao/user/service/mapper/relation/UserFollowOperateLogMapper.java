package org.doubao.user.service.mapper.relation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.user.service.entity.relation.UserFollowOperateLog;

/**
 * 关注操作日志表Mapper
 */
@Mapper
public interface UserFollowOperateLogMapper extends BaseMapper<UserFollowOperateLog> {
}
