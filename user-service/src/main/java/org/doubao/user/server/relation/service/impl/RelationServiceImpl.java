package org.doubao.user.server.relation.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.PageResult;
import org.doubao.user.server.core.service.UserService;
import org.doubao.user.server.relation.entity.UserFollowOperateLog;
import org.doubao.user.server.relation.entity.UserRelation;
import org.doubao.user.server.relation.entity.UserRelationCount;
import org.doubao.user.server.relation.enums.OperateType;
import org.doubao.user.server.relation.enums.RelationType;
import org.doubao.user.server.relation.mapper.UserFollowOperateLogMapper;
import org.doubao.user.server.relation.mapper.UserRelationCountMapper;
import org.doubao.user.server.relation.mapper.UserRelationMapper;
import org.doubao.user.server.relation.service.RelationCacheService;
import org.doubao.user.server.relation.service.RelationService;
import org.doubao.user.server.relation.service.UserPrivacyService;
import org.doubao.user.server.relation.util.UserValidator;
import org.doubao.user.server.relation.vo.FollowResult;
import org.doubao.user.server.relation.vo.PrivacySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 关系服务实现类
 */
@Service
public class RelationServiceImpl extends ServiceImpl<UserRelationMapper, UserRelation> implements RelationService {

	private static final Logger log = LoggerFactory.getLogger(RelationServiceImpl.class);
	@Resource
	private UserService userService;
	@Resource
	private UserRelationMapper userRelationMapper;
	@Resource
	private UserRelationCountMapper countMapper;
	@Resource
	private UserFollowOperateLogMapper logMapper;
	@Resource
	private UserValidator userValidator; // 校验用户状态（是否存在/活跃）
	@Resource
	private RedisTemplate<String, Object> redisTemplate; // 缓存工具
	@Resource
	private RabbitTemplate rabbitTemplate; // 消息队列工具
	@Resource
	private UserPrivacyService userPrivacyService;
	@Resource
	private RelationCacheService relationCacheService;
	// 批量操作最大数量限制
	private static final int MAX_BATCH_SIZE = 100;
	// Redis缓存键前缀
	private static final String FOLLOWING_COUNT_KEY = "user:following_count:%d";
	private static final String FOLLOWER_COUNT_KEY = "user:follower_count:%d";
	private static final String FOLLOWER_CACHE_KEY = "user:followers:%d:page:%d:size:%d";
	private static final String FOLLOWING_CACHE_KEY = "user:following:%d:page:%d:size:%d";
	private static final String FOLLOWING_CACHE_KEY_PREFIX = "user:following:%d";
	private static final String FOLLOWER_CACHE_KEY_PREFIX = "user:followers:%d";
	private static final int CACHE_TTL_SECONDS = 300; // 缓存5分钟


	/**
	 * 关注用户
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void follow(Long targetUserId) {
		// 获取当前用户ID
		Long userId = UserContext.getUserId();
		// 1. 校验用户状态（必须存在且活跃）
		userValidator.validateActiveUser(userId);
		userValidator.validateActiveUser(targetUserId);

		// 2. 校验不能关注自己
		if (userId.equals(targetUserId)) {
			throw new BusinessException(ErrorCode.CANNOT_FOLLOW_YOURSELF);
		}

		// 3. 检查是否已关注
		int exists = userRelationMapper.existsRelation(
				userId, targetUserId, RelationType.FOLLOW.getValue()
		);
		if (exists > 0) {
			throw new BusinessException(ErrorCode.USER_ALREADY_FOLLOWED);
		}

		// 4. 创建关注关系记录
		UserRelation relation = new UserRelation();
		relation.setUserId(userId);
		relation.setTargetUserId(targetUserId);
		relation.setRelationType(RelationType.FOLLOW.getValue());
		relation.setIsMutual(0); // 初始为非互关
		relation.setCreatedTime(LocalDateTime.now());
		relation.setUpdatedTime(LocalDateTime.now());
		userRelationMapper.insert(relation);

		// 5. 检查目标用户是否关注了当前用户（更新互关状态）
		int targetFollowsUser = userRelationMapper.existsRelation(
				targetUserId, userId, RelationType.FOLLOW.getValue()
		);
		if (targetFollowsUser > 0) {
			// 更新当前关系为互关
			relation.setIsMutual(1);
			userRelationMapper.updateById(relation);
			// 双方互关数+1
			countMapper.incrementMutualCount(userId, 1);
			countMapper.incrementMutualCount(targetUserId, 1);
		}

		// 6. 更新关注数和粉丝数
		updateFollowingCount(userId, 1); // 关注数+1
		updateFollowerCount(targetUserId, 1); // 粉丝数+1

		// 7. 记录操作日志（支持撤销）
		recordOperateLog(userId, OperateType.FOLLOW.getValue(), Collections.singletonList(targetUserId));

		// 8. 发布关注事件（通知/动态流等服务消费）
		// rabbitTemplate.convertAndSend(
		// 		"relation.exchange",
		// 		"relation.follow",
		// 		new FollowEvent(userId, targetUserId, System.currentTimeMillis())
		// );

		//  9. 删除缓存
		clearCache(userId, targetUserId);

		log.info("用户 {} 关注了用户 {}", userId, targetUserId);
	}

	private void clearCache(Long userId, Long targetUserId) {
		relationCacheService.deleteCacheBatch(String.format(FOLLOWING_CACHE_KEY_PREFIX, userId));
		relationCacheService.deleteCacheBatch(String.format(FOLLOWING_CACHE_KEY_PREFIX, targetUserId));
		relationCacheService.deleteCacheBatch(String.format(FOLLOWER_CACHE_KEY_PREFIX, userId));
		relationCacheService.deleteCacheBatch(String.format(FOLLOWER_CACHE_KEY_PREFIX, targetUserId));
		relationCacheService.deleteCache(String.format(FOLLOWING_COUNT_KEY, userId));
		relationCacheService.deleteCache(String.format(FOLLOWING_COUNT_KEY, targetUserId));
		relationCacheService.deleteCache(String.format(FOLLOWER_COUNT_KEY, userId));
		relationCacheService.deleteCache(String.format(FOLLOWER_COUNT_KEY, targetUserId));
		relationCacheService.deleteCache(String.format(FOLLOWER_COUNT_KEY, targetUserId));
		relationCacheService.deleteCache(String.format(FOLLOWER_COUNT_KEY, targetUserId));
	}
	/**
	 * 取消关注
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void unfollow(Long targetUserId) {
		//  获取当前用户ID
		Long userId = UserContext.getUserId();


		// 1. 校验用户状态
		userValidator.validateActiveUser(userId);
		userValidator.validateActiveUser(targetUserId);

		// 2. 检查是否存在关注关系
		int exists = userRelationMapper.existsRelation(
				userId, targetUserId, RelationType.FOLLOW.getValue()
		);
		if (exists == 0) {
			throw new BusinessException(ErrorCode.USER_NOT_FOLLOWED);
		}

		// 3. 查询原关系是否为互关
		LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
		query.eq(UserRelation::getUserId, userId)
				.eq(UserRelation::getTargetUserId, targetUserId)
				.eq(UserRelation::getRelationType, RelationType.FOLLOW.getValue());
		UserRelation relation = userRelationMapper.selectOne(query);
		boolean wasMutual = relation.getIsMutual() == 1;

		// 4. 删除关注关系
		userRelationMapper.delete(query);

		// 5. 若原是互关，更新双方互关数
		if (wasMutual) {
			countMapper.incrementMutualCount(userId, -1);
			countMapper.incrementMutualCount(targetUserId, -1);
		}

		// 6. 更新关注数和粉丝数
		updateFollowingCount(userId, -1); // 关注数-1
		updateFollowerCount(targetUserId, -1); // 粉丝数-1

		// 7. 记录操作日志
		recordOperateLog(userId, OperateType.UNFOLLOW.getValue(), Collections.singletonList(targetUserId));

		// 8. 发布取消关注事件
		// rabbitTemplate.convertAndSend(
		// 		"relation.exchange",
		// 		"relation.unfollow",
		// 		new FollowEvent(userId, targetUserId, System.currentTimeMillis())
		// );

		// 9. 删除缓存
		clearCache(userId, targetUserId);

		log.info("用户 {} 取消关注了用户 {}", userId, targetUserId);
	}

	/**
	 * 批量关注
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchFollow(List<Long> targetUserIds) {
		//  获取当前用户ID
		Long userId = UserContext.getUserId();

		// 1. 校验参数
		if (targetUserIds == null || targetUserIds.isEmpty()) {
			throw new BusinessException(ErrorCode.USER_TARGET_LIST_EMPTY);
		}
		if (targetUserIds.size() > MAX_BATCH_SIZE) {
			throw new BusinessException(ErrorCode.USER_BATCH_FOLLOW_LIMIT);
		}

		// 2. 校验用户状态
		userValidator.validateActiveUser(userId);
		targetUserIds.forEach(userValidator::validateActiveUser);

		// 3. 过滤无效用户（自己/已关注）
		List<Long> validTargets = targetUserIds.stream()
				.filter(targetId -> !userId.equals(targetId)) // 排除自己
				.filter(targetId -> userRelationMapper.existsRelation(
						userId, targetId, RelationType.FOLLOW.getValue()
				) == 0) // 排除已关注
				.distinct() // 去重
				.collect(Collectors.toList());

		if (validTargets.isEmpty()) {
			throw new BusinessException(ErrorCode.USER_NO_FOLLOW_TARGET);
		}

		// 4. 批量创建关注关系
		List<UserRelation> relations = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		for (Long targetId : validTargets) {
			UserRelation relation = new UserRelation();
			relation.setUserId(userId);
			relation.setTargetUserId(targetId);
			relation.setRelationType(RelationType.FOLLOW.getValue());
			relation.setIsMutual(0);
			relation.setCreatedTime(now);
			relation.setUpdatedTime(now);
			relations.add(relation);
		}
		userRelationMapper.batchInsert(relations);

		// 5. 批量更新互关状态（目标用户已关注当前用户的情况）
		int mutualCount = 0;
		for (UserRelation relation : relations) {
			Long targetId = relation.getTargetUserId();
			int targetFollowsUser = userRelationMapper.existsRelation(
					targetId, userId, RelationType.FOLLOW.getValue()
			);
			if (targetFollowsUser > 0) {
				relation.setIsMutual(1);
				userRelationMapper.updateById(relation);
				mutualCount++;
			}
		}
		// 批量更新互关数
		if (mutualCount > 0) {
			countMapper.incrementMutualCount(userId, mutualCount);
			countMapper.incrementMutualCount(userId, mutualCount); // 目标用户合计+mutualCount
		}

		// 6. 批量更新计数
		int delta = validTargets.size();
		updateFollowingCount(userId, delta); // 关注数+delta
		validTargets.forEach(targetId -> updateFollowerCount(targetId, 1)); // 每个目标粉丝数+1

		// 7. 记录批量操作日志
		recordOperateLog(userId, OperateType.BATCH_FOLLOW.getValue(), validTargets);

		// 8. 批量发布关注事件
		// validTargets.forEach(targetId -> rabbitTemplate.convertAndSend(
		// 		"relation.exchange",
		// 		"relation.follow",
		// 		new FollowEvent(userId, targetId, System.currentTimeMillis())
		// ));

		// 9.  删除缓存
		relationCacheService.deleteCacheBatch(String.format(FOLLOWING_CACHE_KEY_PREFIX, userId));
		relationCacheService.deleteCache(String.format(FOLLOWING_COUNT_KEY, userId));

		validTargets.forEach(targetId -> relationCacheService.deleteCache(String.format(FOLLOWER_CACHE_KEY_PREFIX, targetId)));
		validTargets.forEach(targetId -> relationCacheService.deleteCache(String.format(FOLLOWER_COUNT_KEY, targetId)));

		log.info("用户 {} 批量关注了 {} 个用户", userId, validTargets.size());
	}

	/**
	 * 获取粉丝列表（分页）
	 */
	@Override
	public PageResult<UserInfo> getFollowers(Long targetUserId, int page, int size) {
		// 1. 校验参数
		validatePageParams(page, size);
		userValidator.validateActiveUser(targetUserId); // 校验用户存在
		Long currentUserId = UserContext.getUserId();
		// 2. 检查粉丝列表隐私设置
		if (!userPrivacyService.checkSeePermission(targetUserId, currentUserId, PrivacySettings.SeeAccessType.FOLLOWERS)) {
			throw new BusinessException(ErrorCode.USER_PRIVACY_FOLLOWER_LIST_NOT_OPEN);
		}

		// 3. 计算分页偏移量
		int offset = (page - 1) * size;

		// 4. 尝试从缓存获取
		String cacheKey = String.format(FOLLOWER_CACHE_KEY, targetUserId, page, size);
		PageResult<UserInfo> cachedResult = relationCacheService.getFollowerPage(cacheKey);
		if (cachedResult != null) {
			return cachedResult;
		}

		// 5. 缓存未命中，查询数据库
		List<Long> followerIds = userRelationMapper.selectFollowerIds(
				targetUserId,
				RelationType.FOLLOW.getValue(),
				offset,
				size
		);
		long total = userRelationMapper.countFollowers(
				targetUserId,
				RelationType.FOLLOW.getValue()
		);

		List<UserInfo> followers = new ArrayList<>();
		if (!followerIds.isEmpty()) {
			Set<Long> followerIdSet = new HashSet<>(followerIds);
			followers = userService.usersByIds(followerIdSet);
			Map<Long, Boolean> follow = isFollow(targetUserId, followerIdSet);
			for (UserInfo follower : followers) {
				follower.setFollow(follow.getOrDefault(Long.valueOf(follower.getId()), false));
			}
		}

		// 6. 封装分页结果
		PageResult<UserInfo> result = PageResult.of(page, size, total, followers);

		// 7. 缓存结果
		relationCacheService.setFollowerPage(cacheKey, result, CACHE_TTL_SECONDS);

		log.info("用户 {} 的粉丝列表查询完成，页码：{}，条数：{}", targetUserId, page, size);
		return result;
	}

	/**
	 * 获取关注列表（分页）
	 */
	@Override
	public PageResult<UserInfo> getFollowing(Long targetUserId, int page, int size) {
		// 1. 校验参数
		validatePageParams(page, size);
		userValidator.validateActiveUser(targetUserId);
		Long currentUserId = UserContext.getUserId();
		// 2. 检查粉丝列表隐私设置
		if (!userPrivacyService.checkSeePermission(targetUserId, currentUserId, PrivacySettings.SeeAccessType.FOLLOWING)) {
			throw new BusinessException(ErrorCode.USER_PRIVACY_FOLLOWING_LIST_NOT_OPEN);
		}
		// 2. 计算分页偏移量
		int offset = (page - 1) * size;

		// 3. 尝试从缓存获取
		String cacheKey = String.format(FOLLOWING_CACHE_KEY, targetUserId, page, size);
		PageResult<UserInfo> cachedResult = relationCacheService.getFollowingPage(cacheKey);
		if (cachedResult != null) {
			return cachedResult;
		}

		// 4. 缓存未命中，查询数据库
		List<Long> followingIds = userRelationMapper.selectFollowingIds(
				targetUserId,
				RelationType.FOLLOW.getValue(),
				offset,
				size
		);
		long total = userRelationMapper.countFollowing(
				targetUserId,
				RelationType.FOLLOW.getValue()
		);

		List<UserInfo> followings = new ArrayList<>();
		if (!followingIds.isEmpty()) {
			Set<Long> followingIdSet = new HashSet<>(followingIds);
			followings = userService.usersByIds(followingIdSet);
		}

		// 5. 封装分页结果
		PageResult<UserInfo> result = PageResult.of(page, size, total, followings);

		// 6. 缓存结果
		relationCacheService.setFollowingPage(cacheKey, result, CACHE_TTL_SECONDS);

		log.info("用户 {} 的关注列表查询完成，页码：{}，条数：{}", targetUserId, page, size);
		return result;
	}

	@Override
	public Map<String, Integer> getRelationCounts(Long userId) {
		userValidator.validateActiveUser(userId);

		// 1. 尝试从缓存获取
		Integer followerCount = getFollowerCount(userId);
		Integer followingCount = getFollowingCount(userId);

		// 2. 封装结果
		Map<String, Integer> result = new HashMap<>(2);
		result.put("followerCount", followerCount);
		result.put("followingCount", followingCount);
		return result;
	}
	// 获取粉丝数（带缓存）
	@Override
	public int getFollowerCount(Long userId) {
		// 1. 缓存查询
		String cacheKey = String.format(FOLLOWER_COUNT_KEY, userId);
		Integer cachedCount = relationCacheService.getInteger(cacheKey);
		if (cachedCount != null) {
			return cachedCount;
		}

		// 2. 数据库查询
		UserRelationCount count = countMapper.selectById(userId);
		int followerCount = count == null ? 0 : count.getFollowerCount();

		// 3. 缓存结果
		relationCacheService.set(cacheKey, followerCount, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
		return followerCount;
	}

	// 获取关注数（带缓存）
	@Override
	public int getFollowingCount(Long userId) {
		// 1. 缓存查询
		String cacheKey = String.format(FOLLOWING_COUNT_KEY, userId);
		Integer cachedCount = relationCacheService.getInteger(cacheKey);
		if (cachedCount != null) {
			return cachedCount;
		}

		// 2. 数据库查询
		UserRelationCount count = countMapper.selectById(userId);
		int followingCount = count == null ? 0 : count.getFollowingCount();

		// 3. 缓存结果
		relationCacheService.set(cacheKey, followingCount, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
		return followingCount;
	}

	@Override
	public Map<Long, Boolean> isFollow(Long currentUserId, Set<Long> userIds) {
		userValidator.validateActiveUser(currentUserId);
		// 批量查询currentUserId 与 userIds 的关注关系
		Map<Long, Boolean> resultMap = new HashMap<>();
		List<FollowResult> followRaw = userRelationMapper.isFollowRaw(currentUserId, userIds);
		// 填充查询结果
		followRaw.forEach(result ->
				resultMap.put(result.getTargetUserId(), result.getFollow()));

		// 为未查询到的用户设置默认值 false
		userIds.forEach(userId ->
				resultMap.putIfAbsent(userId, false));
		log.info("用户 {} 的关注状态查询完成", currentUserId);
		return resultMap;
	}

	@Override
	public boolean existsFollowRelation(Long targetUserId) {
		Long userId = UserContext.getUserId();
		return userRelationMapper.existsRelation(userId, targetUserId, RelationType.FOLLOW.getValue()) > 0;
	}

	/**
	 * 校验分页参数合法性
	 */
	private void validatePageParams(int page, int size) {
		if (page < 1) {
			throw new BusinessException(ErrorCode.PAGE_NUMBER_INVALID);
		}
		if (size < 1 || size > 100) {
			throw new BusinessException(ErrorCode.PAGE_SIZE_INVALID);
		}
	}


	/**
	 * 更新关注数（含缓存）
	 */
	private void updateFollowingCount(Long userId, int delta) {
		// 1. 查询计数记录，不存在则创建
		UserRelationCount count = countMapper.selectById(userId);
		if (count == null) {
			count = new UserRelationCount();
			count.setUserId(userId);
			count.setFollowingCount(delta);
			count.setFollowerCount(0);
			count.setMutualCount(0);
			count.setUpdatedTime(LocalDateTime.now());
			countMapper.insert(count);
		} else {
			// 2. 增量更新
			countMapper.incrementFollowingCount(userId, delta);
			count.setFollowingCount(count.getFollowingCount() + delta);
		}
		// 3. 更新Redis缓存
		redisTemplate.opsForValue().set(
				String.format(FOLLOWING_COUNT_KEY, userId),
				count.getFollowingCount()
		);
	}

	/**
	 * 更新粉丝数（含缓存）
	 */
	private void updateFollowerCount(Long userId, int delta) {
		UserRelationCount count = countMapper.selectById(userId);
		if (count == null) {
			count = new UserRelationCount();
			count.setUserId(userId);
			count.setFollowingCount(0);
			count.setFollowerCount(delta);
			count.setMutualCount(0);
			count.setUpdatedTime(LocalDateTime.now());
			countMapper.insert(count);
		} else {
			countMapper.incrementFollowerCount(userId, delta);
			count.setFollowerCount(count.getFollowerCount() + delta);
		}
		redisTemplate.opsForValue().set(
				String.format(FOLLOWER_COUNT_KEY, userId),
				count.getFollowerCount()
		);
	}

	/**
	 * 记录操作日志（支持撤销）
	 */
	private void recordOperateLog(Long userId, int operateType, List<Long> targetUserIds) {
		UserFollowOperateLog log = new UserFollowOperateLog();
		log.setUserId(userId);
		log.setOperateType(operateType);
		log.setTargetUserIds(JSON.toJSONString(targetUserIds)); // 存储为JSON字符串
		log.setOperateTime(LocalDateTime.now());
		log.setStatus(1); // 状态：有效
		logMapper.insert(log);
	}
}