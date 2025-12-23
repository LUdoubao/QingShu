package org.doubao.user.service.service.impl.relation;

import com.alibaba.fastjson.JSON;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.user.service.entity.relation.UserPrivacy;
import org.doubao.user.service.enums.RelationType;
import org.doubao.user.service.mapper.relation.UserBlockMapper;
import org.doubao.user.service.mapper.relation.UserPrivacyMapper;
import org.doubao.user.service.mapper.relation.UserRelationMapper;
import org.doubao.user.service.service.relation.UserPrivacyService;
import org.doubao.user.service.utils.UserValidator;
import org.doubao.user.service.vo.relation.PrivacySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 隐私服务实现：核心逻辑为"可见性校验+关系判断"
 */
@Service
public class UserPrivacyServiceImpl implements UserPrivacyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserPrivacyServiceImpl.class);
	private static final String FOLLOWER_VISIBILITY = "followerVisibility";
	private static final String FOLLOWING_VISIBILITY = "followingVisibility";

	@Resource
	private UserPrivacyMapper privacyMapper;
	@Resource
	private UserRelationMapper relationMapper; // 依赖关系表查询互关状态
	@Resource
	private UserValidator userValidator;

	@Resource
	private UserBlockMapper userBlockMapper;
	@Override
	public boolean checkSeePermission(Long targetUserId, Long currentUserId, int seeAccessType) {
		// 检查是否被拉黑
		Integer count = userBlockMapper.checkBlockRelation(targetUserId, currentUserId);
		if (count != null && count > 0) {
			return false;
		}
		// 1. 获取目标用户的隐私设置（默认公开）
		PrivacySettings settings = getSettings(targetUserId);
		LOGGER.info("=============getSettings settings: {} " , JSON.toJSONString(settings));
		Integer visibility;
		switch (seeAccessType) {
			case PrivacySettings.SeeAccessType.PROFILE:
				visibility = settings.getProfileVisibility();
				break;
		    case PrivacySettings.SeeAccessType.FOLLOWERS:
				visibility = settings.getFollowerVisibility();
			    break;
			case PrivacySettings.SeeAccessType.FOLLOWING:
				visibility = settings.getFollowingVisibility();
			    break;
			case PrivacySettings.SeeAccessType.WORK:
				visibility = settings.getWorkVisibility();
			    break;
			case PrivacySettings.SeeAccessType.CHAT:
				visibility = settings.getChatVisibility();
			    break;
			default:
				return true;
		}

		// 2. 权限判断逻辑
		switch (visibility) {
			case 1: // 公开：所有人
				return true;
			case 2: // 仅互关：需判断当前用户与目标用户是否互关
				if (currentUserId == null) { // 未登录用户无权限
					return false;
				}
				if (currentUserId.equals(targetUserId)) { // 本人永远有权限
					return true;
				}
				// 查询是否互关（A关注B且B关注A）
				int aFollowsB = relationMapper.existsRelation(
						currentUserId, targetUserId, RelationType.FOLLOW.getValue()
				);
				int bFollowsA = relationMapper.existsRelation(
						targetUserId, currentUserId, RelationType.FOLLOW.getValue()
				);
				return aFollowsB > 0 && bFollowsA > 0;
			case 3: // 私密：仅本人可看
				return currentUserId != null && currentUserId.equals(targetUserId);
			case 4: // 仅关注自己的用户
				if (currentUserId == null) { // 未登录用户无权限
					return false;
				}
				if (currentUserId.equals(targetUserId)) { // 本人永远有权限
					return true;
				}
				return relationMapper.existsRelation(
						currentUserId, targetUserId, RelationType.FOLLOW.getValue()
				) > 0;
			default:
				throw new BusinessException(ErrorCode.USER_INVALID_VISIBILITY);
		}
	}

	@Override
	public void updateFollowerVisibility(PrivacySettings privacySettings) {
		Integer followingVisibility = privacySettings.getFollowingVisibility();
		Integer followerVisibility = privacySettings.getFollowerVisibility();
		Integer profileVisibility = privacySettings.getProfileVisibility();
		Integer workVisibility = privacySettings.getWorkVisibility();
		Integer chatVisibility = privacySettings.getChatVisibility();
		Long userId = UserContext.getUserId();

		// 1. 校验参数合法性
		if (followingVisibility < 1 || followingVisibility > 4) {
			throw new BusinessException(ErrorCode.USER_INVALID_VISIBILITY);
		}
		if (followerVisibility < 1 || followerVisibility > 4) {
			throw new BusinessException(ErrorCode.USER_INVALID_VISIBILITY);
		}
		if (profileVisibility < 1 || profileVisibility > 4) {
			throw new BusinessException(ErrorCode.USER_INVALID_VISIBILITY);
		}
		if (workVisibility < 1 || workVisibility > 4) {
			throw new BusinessException(ErrorCode.USER_INVALID_VISIBILITY);
		}
		if (chatVisibility < 1 || chatVisibility > 4) {
			throw new BusinessException(ErrorCode.USER_INVALID_VISIBILITY);
		}


		// 2. 查询用户隐私记录（不存在则创建）
		UserPrivacy privacy = privacyMapper.selectByUserId(userId);
		if (privacy == null) {
			privacy = new UserPrivacy();
			privacy.setUserId(userId);
			privacy.setFollowerVisibility(followerVisibility);
			privacy.setFollowingVisibility(followingVisibility);
			privacy.setProfileVisibility(profileVisibility);
			privacy.setWorkVisibility(workVisibility);
			privacy.setChatVisibility(chatVisibility);
			privacyMapper.insert(privacy);
		} else {
			privacy.setFollowerVisibility(followerVisibility);
			privacy.setFollowingVisibility(followingVisibility);
			privacy.setProfileVisibility(profileVisibility);
			privacy.setWorkVisibility(workVisibility);
			privacy.setChatVisibility(chatVisibility);
			privacyMapper.updateById(privacy);
		}
	}

	public PrivacySettings getSettings(Long userId) {
		UserPrivacy privacy = privacyMapper.selectByUserId(userId);
		PrivacySettings settings = new PrivacySettings();
		settings.setFollowerVisibility(privacy == null ? 1 : privacy.getFollowerVisibility());
		settings.setFollowingVisibility(privacy == null ? 1 : privacy.getFollowingVisibility());
		settings.setProfileVisibility(privacy == null ? 1 : privacy.getProfileVisibility());
		settings.setWorkVisibility(privacy == null ? 1 : privacy.getWorkVisibility());
		settings.setChatVisibility(privacy == null ? 1 : privacy.getChatVisibility());
		return settings;
	}

	public PrivacySettings getSettings() {
		Long userId = UserContext.getUserId();
		return getSettings(userId);
	}
}