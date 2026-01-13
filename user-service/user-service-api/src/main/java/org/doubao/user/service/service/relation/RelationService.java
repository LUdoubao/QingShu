package org.doubao.user.service.service.relation;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.vo.PageResult;
import org.doubao.user.service.dto.relation.UserInfoDesFollow;
import org.doubao.user.service.entity.relation.UserRelation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 关系服务接口
 */
public interface RelationService extends IService<UserRelation> {
	// 关注用户
	void follow(Long targetUserId);

	// 取消关注
	void unfollow(Long targetUserId);

	// 批量关注
	void batchFollow(List<Long> targetUserIds);

	/**
	 * 获取粉丝列表（分页）
	 * @param userId 目标用户ID（查询该用户的粉丝）
	 * @param page 页码（从1开始）
	 * @param size 每页条数
	 * @return 分页粉丝列表（包含用户ID）
	 */
	PageResult<UserInfoDesFollow> getFollowers(Long userId, int page, int size);


	List<Long> allFollowers(Long userId);

	/**
	 * 获取关注列表（分页）
	 * @param userId 目标用户ID（查询该用户关注的人）
	 * @param page 页码
	 * @param size 每页条数
	 * @return 分页关注列表（包含用户ID）
	 */
	PageResult<UserInfoDes> getFollowing(Long userId, int page, int size);


	List<Long> allFollows(Long userId);

	/**
	 * 批量获取用户粉丝数
	 * @param userIds 用户 ID列表
	 * @return 粉丝数列表
	 */
	Map<Long, Long> getFollowerCounts(List<Long> userIds);

	/**
	 * 获取用户的粉丝数和关注数
	 * @param userId 目标用户ID
	 * @return 包含followerCount和followingCount的Map
	 */
	Map<String, Integer> getRelationCounts(Long userId);

	/**
	 * 获取用户的粉丝数
	 * @param userId 目标用户ID
	 * @return 粉丝数
	 */
	int getFollowerCount(Long userId);

	/**
	 * 获取用户的关注数
	 * @param userId 目标用户ID
	 * @return 关注数
	 */
	int getFollowingCount(Long userId);

	Map<Long, Boolean> isFollow(Long currentUserId, Set<Long> userIds);

	boolean existsFollowRelation(Long targetUserId);


	void  blockFollowRelation(Long userIdOne, Long userIdTwo);
}
