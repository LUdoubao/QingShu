package org.doubao.user.service.mapper.relation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.service.dto.relation.FollowerCount;
import org.doubao.user.service.entity.relation.UserRelation;
import org.doubao.user.service.vo.relation.FollowResult;

import java.util.List;
import java.util.Set;

/**
 * 用户关系表Mapper
 */
@Mapper
public interface UserRelationMapper extends BaseMapper<UserRelation> {


	int existsRelation(
			@Param("userId") Long userId,
			@Param("targetUserId") Long targetUserId,
			@Param("relationType") Integer relationType
	);

	// 批量插入关系记录
	void batchInsert(@Param("list") List<UserRelation> relations);

	// 批量删除关系记录（取消关注）
	void batchDelete(
			@Param("userId") Long userId,
			@Param("targetUserIds") List<Long> targetUserIds,
			@Param("relationType") Integer relationType
	);


	List<Long> selectFollowerIds(
			@Param("targetUserId") Long targetUserId,
			@Param("relationType") Integer relationType,
			@Param("offset") int offset,
			@Param("size") int size
	);

	List<Long> selectAllFollowerIds(
			@Param("targetUserId") Long targetUserId,
			@Param("relationType") Integer relationType
	);


	List<Long> selectFollowingIds(
			@Param("userId") Long userId,
			@Param("relationType") Integer relationType,
			@Param("offset") int offset,
			@Param("size") int size
	);

	List<Long> selectAllFollowingIds(
			@Param("userId") Long userId,
			@Param("relationType") Integer relationType
	);

	/**
	 * 统计粉丝总数
	 * @param targetUserId 被关注用户ID
	 * @param relationType 关系类型
	 * @return 粉丝总数
	 */
	long countFollowers(
			@Param("targetUserId") Long targetUserId,
			@Param("relationType") Integer relationType
	);

	/**
	 * 统计关注总数
	 * @param userId 关注者ID
	 * @param relationType 关系类型
	 * @return 关注总数
	 */
	long countFollowing(
			@Param("userId") Long userId,
			@Param("relationType") Integer relationType
	);

	List<FollowResult> isFollowRaw(@Param("currentUserId") Long currentUserId,
								   @Param("userIds") Set<Long> userIds);

	List<FollowerCount> getFollowerCounts(@Param("userIds") List<Long> userIds);
}
