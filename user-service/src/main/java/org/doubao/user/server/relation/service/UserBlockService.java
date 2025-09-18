package org.doubao.user.server.relation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.vo.PageResult;
import org.doubao.user.server.relation.entity.UserBlock;
import org.doubao.user.server.relation.vo.BlockedUserVO;

import java.util.List;
import java.util.Map;

/**
 * 黑名单服务接口
 */
public interface UserBlockService extends IService<UserBlock> {
	/**
	 * 拉黑用户
	 * @param userId 当前用户ID
	 * @param targetUserId 目标用户ID
	 */
	void blockUser(Long userId, Long targetUserId);

	/**
	 * 解除拉黑
	 * @param userId 当前用户ID
	 * @param targetUserId 目标用户ID
	 */
	void unblockUser(Long userId, Long targetUserId);

	/**
	 * 获取黑名单列表
	 * @param userId 当前用户ID
	 * @return 黑名单用户列表
	 */
	PageResult<BlockedUserVO> getBlockList(Long userId, int page, int size);

	Integer getBlockCount(Long userId);

	/**
	 * 检查是否被拉黑
	 * @param userId 当前用户ID
	 * @param targetUserId 目标用户ID
	 * @return 是否被拉黑
	 */
	boolean checkIsBlocked(Long userId, Long targetUserId);

	Map<Long, Boolean> checkBatch(Long userId, List<Long> targetUserIds);
}