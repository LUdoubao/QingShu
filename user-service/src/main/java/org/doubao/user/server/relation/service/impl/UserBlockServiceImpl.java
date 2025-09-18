package org.doubao.user.server.relation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.vo.PageResult;
import org.doubao.user.server.core.feign.OssServiceClient;
import org.doubao.user.server.relation.entity.UserBlock;
import org.doubao.user.server.relation.mapper.UserBlockMapper;
import org.doubao.user.server.relation.service.RelationService;
import org.doubao.user.server.relation.service.UserBlockService;
import org.doubao.user.server.relation.vo.BlockCheckVo;
import org.doubao.user.server.relation.vo.BlockedUserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 黑名单服务实现类
 */
@Service
public class UserBlockServiceImpl extends ServiceImpl<UserBlockMapper, UserBlock> implements UserBlockService {

	@Resource
	private UserBlockMapper userBlockMapper;
	@Resource
	private OssServiceClient ossServiceClient;
	@Resource
	private RelationService relationService;
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void blockUser(Long userId, Long targetUserId) {
		// 不能拉黑自己
		if (userId.equals(targetUserId)) {
			throw new BusinessException(ErrorCode.USER_BLOCK_SELF);
		}

		// 检查是否已拉黑
		Integer count = userBlockMapper.checkBlockRelation(userId, targetUserId);
		if (count != null && count > 0) {
			throw new BusinessException(ErrorCode.USER_BLOCK_EXISTS);
		}

		// 执行拉黑
		UserBlock userBlock = new UserBlock();
		userBlock.setUserId(userId);
		userBlock.setBlockedUserId(targetUserId);
		userBlock.setCreatedTime(LocalDateTime.now());
		baseMapper.insert(userBlock);
		// 删除互相的关注关系
		relationService.blockFollowRelation(userId, targetUserId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void unblockUser(Long userId, Long targetUserId) {
		// 检查是否存在拉黑关系
		Integer count = userBlockMapper.checkBlockRelation(userId, targetUserId);
		if (count == null || count == 0) {
			throw new BusinessException(ErrorCode.USER_BLOCK_NOT_EXISTS);
		}

		LambdaQueryWrapper<UserBlock> queryWrapper = new LambdaQueryWrapper<UserBlock>()
				.eq(UserBlock::getUserId, userId)
				.eq(UserBlock::getBlockedUserId, targetUserId);
		// 执行解除拉黑
		baseMapper.delete(queryWrapper);
	}

	@Override
	public PageResult<BlockedUserVO> getBlockList(Long userId, int page, int size) {
		Integer blockCount = userBlockMapper.getBlockCount(userId);
		int offset = (page - 1) * size;
		List<BlockedUserVO> list = userBlockMapper.selectBlockedUserList(userId, offset, size);
		if (list != null && !list.isEmpty()) {
			for (BlockedUserVO blockedUserVO : list) {
				// 动态生成头像URL
				blockedUserVO.setAvatarUrl(ossServiceClient.generateAccessUrl(
						blockedUserVO.getAvatarUrl(),
						blockedUserVO.getStorageType()
				).getData());
			}
			return PageResult.of(page, size, blockCount, list);
		}
		return new PageResult<>();
	}

	@Override
	public Integer getBlockCount(Long userId) {
		return userBlockMapper.getBlockCount(userId);
	}

	@Override
	public boolean checkIsBlocked(Long userId, Long targetUserId) {
		Integer count = userBlockMapper.checkBlockRelation(userId, targetUserId);
		return count != null && count > 0;
	}

	@Override
	public Map<Long, Boolean> checkBatch(Long userId, List<Long> targetUserIds) {
		// 批量检查是否拉黑目标用户
		if (targetUserIds == null || targetUserIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<BlockCheckVo> blockCheckVos = userBlockMapper.checkBatch(userId, targetUserIds);
		return blockCheckVos.stream().collect(
				Collectors.toMap(BlockCheckVo::getTargetUserId, BlockCheckVo::isBlocked)
		);
	}
}