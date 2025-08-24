package org.doubao.user.server.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.relation.entity.UserRelationCount;

/**
 * 关系计数表Mapper
 */
@Mapper
public interface UserRelationCountMapper extends BaseMapper<UserRelationCount> {

	// 增量更新关注数
	void incrementFollowingCount(@Param("userId") Long userId, @Param("delta") int delta);

	// 增量更新粉丝数
	void incrementFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);

	// 增量更新互关数
	void incrementMutualCount(@Param("userId") Long userId, @Param("delta") int delta);
}
