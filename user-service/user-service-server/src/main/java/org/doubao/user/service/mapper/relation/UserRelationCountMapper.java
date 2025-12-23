package org.doubao.user.service.mapper.relation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.service.entity.relation.UserRelationCount;

/**
 * 关系计数表Mapper
 */
@Mapper
public interface UserRelationCountMapper extends BaseMapper<UserRelationCount> {


	void incrementFollowingCount(@Param("userId") Long userId, @Param("delta") int delta);


	void incrementFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);

	void incrementMutualCount(@Param("userId") Long userId, @Param("delta") int delta);
}
