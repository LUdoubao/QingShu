package org.doubao.user.server.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.relation.entity.UserRelation;
import org.doubao.user.server.relation.vo.FollowResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户关系表Mapper
 */
@Mapper
public interface UserRelationMapper extends BaseMapper<UserRelation> {

	// 检查关系是否存在
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

	/**
	 * 查询粉丝ID列表（分页）
	 * @param targetUserId 被关注用户ID（粉丝所属的用户）
	 * @param relationType 关系类型（1=关注）
	 * @param offset 偏移量（(page-1)*size）
	 * @param size 每页条数
	 * @return 粉丝用户ID列表
	 */
	List<Long> selectFollowerIds(
			@Param("targetUserId") Long targetUserId,
			@Param("relationType") Integer relationType,
			@Param("offset") int offset,
			@Param("size") int size
	);

	/**
	 * 查询关注列表ID（分页）
	 * @param userId 关注者ID
	 * @param relationType 关系类型（1=关注）
	 * @param offset 偏移量
	 * @param size 每页条数
	 * @return 关注的用户ID列表
	 */
	List<Long> selectFollowingIds(
			@Param("userId") Long userId,
			@Param("relationType") Integer relationType,
			@Param("offset") int offset,
			@Param("size") int size
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
}