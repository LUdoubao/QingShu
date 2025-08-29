package org.doubao.like.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.like.service.dto.LikeQueryDto;
import org.doubao.like.service.dto.request.BatchLikeStatusRequest;
import org.doubao.like.service.dto.request.ToggleLikeRequest;
import org.doubao.like.service.dto.response.BatchLikeStatusResponse;
import org.doubao.like.service.dto.response.HotContentResponse;
import org.doubao.like.service.dto.response.ToggleLikeResponse;
import org.doubao.like.service.entity.LikeCount;
import org.doubao.like.service.entity.LikeRecord;
import org.doubao.like.service.enums.EntityTypeEnum;
import org.doubao.like.service.enums.LikeAction;
import org.doubao.like.service.exception.RateLimitException;
import org.doubao.like.service.feign.QuoteServiceClient;
import org.doubao.like.service.mapper.LikeCountMapper;
import org.doubao.like.service.mapper.LikeRecordMapper;
import org.doubao.like.service.messaging.LikeEventPublisher;
import org.doubao.like.service.service.LikeService;
import org.doubao.like.service.utils.RateLimiterUtil;
import org.doubao.like.service.utils.RedisKeyUtil;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.ResultCode;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// LikeServiceImpl.java
@Service
public class LikeServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeService  {
	private static final Logger LOGGER = LoggerFactory.getLogger(LikeServiceImpl.class);
	@Resource
	private QuoteServiceClient quoteServiceClient;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private LikeRecordMapper likeRecordMapper;

	@Autowired
	private LikeCountMapper likeCountMapper;
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RateLimiterUtil rateLimiterUtil;

	@Autowired
	private LikeEventPublisher likeEventPublisher;

	@Autowired
	private CommonTaskExecutor taskExecutor;

	@Override
	@Transactional
	public ToggleLikeResponse toggleLike(ToggleLikeRequest request) {
		Long userId = request.getUserId();
		Long operatorUserId = request.getOperatorUserId();

		// 限流检查 (10次/分钟)
		if (!rateLimiterUtil.tryAcquire(
				RedisKeyUtil.getRateLimitKey(operatorUserId),
				10,
				60)) {
			throw new RateLimitException(operatorUserId, RateLimitException.RateLimitType.LIKE_OPERATION);
		}

		String userName = Constants.DEFAULT_USER_NAME;
		String key = Constants.REDIS_USER + operatorUserId;
		if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
			UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(key);
			if (userInfo != null) {
				userName = userInfo.getUsername();
			}
		}

		int entityType = request.getEntityType();
		String entityId = request.getEntityId();

		// 用户点赞状态键
		String userLikeKey = RedisKeyUtil.getUserLikeKey(operatorUserId, EntityTypeEnum.getNameByType(entityType), entityId);
		// 实体计数键
		String countKey = RedisKeyUtil.getEntityLikeCountKey(EntityTypeEnum.getNameByType(entityType), String.valueOf(entityId));

		// 检查当前用户是否已点赞
		Boolean hasLiked = (Boolean) redisTemplate.opsForValue().get(userLikeKey);

		// 操作结果
		ToggleLikeResponse response = new ToggleLikeResponse();
		response.setSuccess(true);

		if (hasLiked != null && hasLiked) {
			// 取消点赞
			redisTemplate.opsForValue().set(userLikeKey, false);
			redisTemplate.opsForValue().decrement(countKey, 1L);

			// 如果是文案，更新热门集合
			if (request.getEntityType() == EntityTypeEnum.CONTENT.getType()) {
				redisTemplate.opsForZSet().incrementScore(
						RedisKeyUtil.getHotContentsKey(),
						entityId.toString(),
						-1
				);
			}

			// 通知 文案所属用户
			likeEventPublisher.pushLikeNotification(userId, entityType, entityId, false, request.getContent(), operatorUserId, userName);


			response.setAction(LikeAction.CANCEL.getName());
		} else {
			// 点赞
			redisTemplate.opsForValue().set(userLikeKey, true);
			redisTemplate.opsForValue().increment(countKey, 1L);

			// 如果是文案，更新热门集合
			if (request.getEntityType() == EntityTypeEnum.CONTENT.getType()) {
				redisTemplate.opsForZSet().incrementScore(
						RedisKeyUtil.getHotContentsKey(),
						entityId.toString(),
						1
				);
			}

			// 发送MQ消息通知文案所属用户
			likeEventPublisher.pushLikeNotification(userId, entityType, entityId, true,request.getContent(), operatorUserId, userName);

			response.setAction(LikeAction.LIKE.getName());
		}
		// 更新点赞记录
		updateLikeRecordAndCount(operatorUserId, entityType, entityId, response);
		// 获取最新的计数
		Object countObj = null;
		if (Boolean.TRUE.equals(redisTemplate.hasKey(countKey))) {
			countObj = redisTemplate.opsForValue().get(countKey);
		}
		if (countObj != null) {
			response.setCurrentCount(Long.valueOf(String.valueOf(countObj)));
		}

		return response;
	}

	private void updateLikeRecordAndCount(Long operatorUserId, int entityType, String entityId, ToggleLikeResponse response) {
		taskExecutor.execute(() -> {
			try {
				// 1. 准备参数
				boolean isLike = LikeAction.LIKE.getName().equals(response.getAction());
				int delta = isLike ? 1 : -1;
				LocalDateTime now = LocalDateTime.now();

				// 2. 使用事务更新双表
				transactionTemplate.execute(status -> {
					try {
						// 2.1 更新点赞记录表
						updateLikeRecord(operatorUserId, entityType, entityId, isLike, now);

						// 2.2 更新点赞计数表
						updateLikeCount(entityType, entityId, delta, now);

						return Boolean.TRUE;
					} catch (Exception e) {
						status.setRollbackOnly();
						throw e;
					}
				});

				// 3. 记录成功日志
				LOGGER.info("双表更新成功|userId:{},entityType:{},entityId:{},action:{}",
						operatorUserId, entityType, entityId, response.getAction());
			} catch (Exception e) {
				LOGGER.error("双表更新失败", e);
				// 4. 失败补偿（记录+告警）
				// compensateService.logFailedUpdate(
				// 		operatorUserId, entityType, entityId,
				// 		response.getAction(), e.getMessage()
				// );
			}
		});
	}
	private void updateLikeRecord(Long userId, int entityType,
								  String entityId, boolean isLike,
								  LocalDateTime now) {
		// 存在则更新，不存在则插入
		likeRecordMapper.insertOrUpdate(
				new LikeRecord(userId, entityType, entityId, isLike ? 1 : 0, now)
		);
	}

	private void updateLikeCount(int entityType, String entityId,
								 int delta, LocalDateTime now) {
		// 使用乐观锁保证并发安全
		int retryTimes = 3;
		while (retryTimes-- > 0) {
			LikeCount current = likeCountMapper.selectOne(
					new LambdaQueryWrapper<LikeCount>()
							.eq(LikeCount::getEntityId, entityId)
							.eq(LikeCount::getEntityType, entityType));

			if (current == null) {
				// 首次记录
				if (likeCountMapper.insert(
						new LikeCount(entityType, entityId,delta > 0 ? 1 : 0, now)) > 0) {
					break;
				}
			} else {
				// 增量更新
				if (likeCountMapper.updateCount(
						entityType,
						entityId,
						delta,
						current.getCount(), // 旧值用于CAS
						now) > 0) {
					break;
				}
			}

			if (retryTimes == 0) {
				throw new BusinessException("点赞计数更新冲突", ErrorCode.LIKE_COUNT_UPDATE_ERROR);
			}
			try {
				Thread.sleep(100); // 短暂等待
			} catch (InterruptedException e) {
				LOGGER.error("线程等待异常", e);
			}
		}
	}
	@Override
	public BatchLikeStatusResponse batchGetLikeStatus(BatchLikeStatusRequest request) {
		// 参数校验（防御性编程）
		if (request == null || request.getUserId() == null || CollectionUtils.isEmpty(request.getEntities())) {
			throw new IllegalArgumentException("请求参数不合法");
		}

		boolean queryCount = request.isQueryCount();
		boolean queryStatus = request.isQueryStatus();
		// 准备批量查询参数（按类型分组）
		Map<Integer, List<String>> entityGroupMap = request.getEntities().stream()
				.collect(Collectors.groupingBy(
						BatchLikeStatusRequest.EntityRequest::getEntityType,
						Collectors.mapping(BatchLikeStatusRequest.EntityRequest::getEntityId, Collectors.toList())
				));

		// 并行查询所有实体类型
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		Map<BatchLikeStatusRequest.EntityRequest, Boolean> statusResultMap = new ConcurrentHashMap<>();
		Map<BatchLikeStatusRequest.EntityRequest, Integer> countResultMap = new ConcurrentHashMap<>();

		entityGroupMap.forEach((entityType, entityIds) -> {
			if (queryStatus) {
				// 并行查询点赞状态
				futures.add(CompletableFuture.runAsync(() -> {
					Map<String, Boolean> statusMap = getLikeStatusWithCache(request.getUserId(), entityType, entityIds);
					if (!statusMap.isEmpty()) {
						entityIds.forEach(id -> {
							if (id != null) {
								BatchLikeStatusRequest.EntityRequest key = new BatchLikeStatusRequest.EntityRequest(entityType, id);
								Boolean status = statusMap.get(id);
								if (status != null) {
									statusResultMap.put(key, status);
								} else {
									statusResultMap.put(key, false);
								}
							}
						});
					} else {
						entityIds.forEach(id -> {
							if (id != null) {
								BatchLikeStatusRequest.EntityRequest key = new BatchLikeStatusRequest.EntityRequest(entityType, id);
								statusResultMap.put(key, false);
							}
						});
					}
				}, taskExecutor));
			}

			if (queryCount) {
				// 并行查询点赞数
				futures.add(CompletableFuture.runAsync(() -> {
					Map<String, Integer> countMap = getLikeCountWithCache(entityType, entityIds);
					if (!countMap.isEmpty()) {
						entityIds.forEach(id -> {
							if (id != null) {
								BatchLikeStatusRequest.EntityRequest key = new BatchLikeStatusRequest.EntityRequest(entityType, id);
								if (countMap.containsKey(id)) {
									int count = countMap.get(id);
									countResultMap.put(key, count);
								} else {
									countResultMap.put(key, 0);
								}
							}
						});
					}
				}, taskExecutor));
			}

		});

		// 等待所有查询完成
		try {
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		} catch (Exception e) {
			LOGGER.error("批量查询失败", e);
			throw new BusinessException("批量查询失败", ErrorCode.LIKE_FOUND_ERROR);
		}

		// 组装结果（保持原始顺序）
		List<BatchLikeStatusResponse.LikeStatusResult> results = request.getEntities().stream()
				.map(entity -> {
					BatchLikeStatusResponse.LikeStatusResult result =  new BatchLikeStatusResponse.LikeStatusResult();
					if (queryStatus) {
						statusResultMap.forEach((key, value) -> {
							if (entity.getEntityType()== (key.getEntityType()) && entity.getEntityId().equals(key.getEntityId())) {
								result.setEntityType(key.getEntityType());
								result.setEntityId(key.getEntityId());
								result.setLiked(value!= null ? value : false);
							}
						});
					}

					if (queryCount) {
						countResultMap.forEach((key, value) -> {
							if (entity.getEntityType() == (key.getEntityType()) && entity.getEntityId().equals(key.getEntityId())) {
								result.setCount(value!= null ? value : 0);
							}
						});
					}
					return result;
				})
				.collect(Collectors.toList());

		return BatchLikeStatusResponse.of(results);
	}

	// 带缓存的点赞状态查询
	private Map<String, Boolean> getLikeStatusWithCache(Long userId, Integer entityType, List<String> entityIds) {
		// 1. 构建Redis keys
		List<String> redisKeys = entityIds.stream()
				.map(id -> RedisKeyUtil.getUserLikeKey(userId, EntityTypeEnum.getNameByType(entityType), id))
				.collect(Collectors.toList());

		// 2. 批量从Redis获取
		List<Object> redisResults = redisTemplate.opsForValue().multiGet(redisKeys);
		Map<String, Boolean> resultMap = new HashMap<>();
		List<String> missingIds = new ArrayList<>();

		// 3. 处理Redis结果
		for (int i = 0; i < entityIds.size(); i++) {
			String entityId = entityIds.get(i);
			if (redisResults == null) {
				missingIds.add(entityId);
				continue;
			}
			Object cached = redisResults.get(i);
			if (cached != null) {
				resultMap.put(entityId, Boolean.TRUE.equals(cached));
			} else {
				missingIds.add(entityId);
			}
		}

		// 4. 查询缺失的DB数据
		if (!missingIds.isEmpty()) {
			Map<String, Boolean> dbResults = getLikeStatusFromDB(userId, entityType, missingIds);
			resultMap.putAll(dbResults);

			// 5. 回填Redis缓存
			dbResults.forEach((id, status) -> {
				String key = RedisKeyUtil.getUserLikeKey(userId, String.valueOf(entityType), id);
				redisTemplate.opsForValue().set(key, status ? "1" : "0", 1, TimeUnit.DAYS);
			});
		}

		return resultMap;
	}

	// 带缓存的点赞数查询
	private Map<String, Integer> getLikeCountWithCache(Integer entityType, List<String> entityIds) {
		// 1. 构建Redis keys
		List<String> redisKeys = entityIds.stream()
				.map(id -> RedisKeyUtil.getEntityLikeCountKey(EntityTypeEnum.getNameByType(entityType), String.valueOf(id)))
				.collect(Collectors.toList());

		// 2. 批量从Redis获取
		List<Object> redisResults = redisTemplate.opsForValue().multiGet(redisKeys);
		Map<String, Integer> resultMap = new HashMap<>();
		List<String> missingIds = new ArrayList<>();

		// 3. 处理Redis结果
		for (int i = 0; i < entityIds.size(); i++) {
			String entityId = entityIds.get(i);
			if (redisResults == null) {
				missingIds.add(entityId);
				continue;
			}
			Object cached = redisResults.get(i);
			if (cached != null) {
				resultMap.put(entityId, Integer.parseInt(cached.toString()));
			} else {
				missingIds.add(entityId);
			}
		}

		// 4. 查询缺失的DB数据
		if (!missingIds.isEmpty()) {
			Map<String, Integer> dbResults = getLikeCountFromDB(entityType, missingIds);
			resultMap.putAll(dbResults);

			// 5. 回填Redis缓存
			dbResults.forEach((id, count) -> {
				String key = RedisKeyUtil.getEntityLikeCountKey(String.valueOf(entityType), String.valueOf(id));
				redisTemplate.opsForValue().set(key, count.toString(), 1, TimeUnit.DAYS);
			});
		}

		return resultMap;
	}

	// 数据库查询方法
	private Map<String, Boolean> getLikeStatusFromDB(Long userId, Integer entityType, List<String> entityIds) {
		if (CollectionUtils.isEmpty(entityIds)) {
			return Collections.emptyMap();
		}

		List<LikeRecord> records = likeRecordMapper.selectList(
				new LambdaQueryWrapper<LikeRecord>()
						.eq(LikeRecord::getUserId, userId)
						.eq(LikeRecord::getEntityType, entityType)
						.in(LikeRecord::getEntityId, entityIds)
		);

		return records.stream()
				.collect(Collectors.toMap(
						LikeRecord::getEntityId,
						record -> record.getLiked() == 1
				));
	}

	private Map<String, Integer> getLikeCountFromDB(Integer entityType, List<String> entityIds) {
		if (CollectionUtils.isEmpty(entityIds)) {
			return Collections.emptyMap();
		}

		List<LikeCount> counts = likeCountMapper.selectList(
				new LambdaQueryWrapper<LikeCount>()
						.eq(LikeCount::getEntityType, entityType)
						.in(LikeCount::getEntityId, entityIds)
		);

		return counts.stream()
				.collect(Collectors.toMap(
						LikeCount::getEntityId,
						LikeCount::getCount
				));
	}

	@Override
	public List<HotContentResponse> getHotContents(int limit) {
		// 参数校验
		if (limit <= 0 || limit > 1000) {
			throw new IllegalArgumentException("limit参数必须在1-1000之间");
		}

		// 1. 从Redis获取热门文案ID列表（ZSET按分数降序）
		Set<ZSetOperations.TypedTuple<Object>> hotContentTuples = redisTemplate.opsForZSet()
				.reverseRangeWithScores(RedisKeyUtil.getHotContentsKey(), 0, limit - 1);

		if (CollectionUtils.isEmpty(hotContentTuples)) {
			return Collections.emptyList();
		}

		// 2. 提取ID和分数（点赞数）
		List<Long> contentIds = new ArrayList<>(hotContentTuples.size());
		Map<Long, Integer> contentScoreMap = new HashMap<>();

		hotContentTuples.forEach(tuple -> {
			Long contentId = Long.parseLong(String.valueOf(tuple.getValue()));
			int score = Objects.requireNonNull(tuple.getScore()).intValue();
			contentIds.add(contentId);
			contentScoreMap.put(contentId, score);
		});

		// 3. Feign批量查询文案基础信息
		Result<List<Map<String, Object>>> quoteResult = quoteServiceClient.getQuotesByIds(contentIds);
		if (quoteResult == null || !Objects.equals(quoteResult.getCode(), ResultCode.SUCCESS.getCode()) || CollectionUtils.isEmpty(quoteResult.getData())) {
			LOGGER.error("获取文案信息失败: {}", quoteResult);
			return Collections.emptyList();
		}

		List<Map<String, Object>> quotes = quoteResult.getData();

		// 4. 获取排名变化数据
		Map<Long, Integer> rankChangeMap = getRankChanges(contentIds);

		// 5. 组装响应数据
		List<Map<String, Object>> hotContents = quotes.stream()
				.map(quote -> {
					Long quoteId = Long.parseLong(quote.get("id").toString());
					Map<String, Object> item = new HashMap<>(quote);
					item.put("likeCount", contentScoreMap.getOrDefault(quoteId, 0));
					item.put("rankChange", rankChangeMap.getOrDefault(quoteId, null));
					return item;
				})
				.collect(Collectors.toList());

		// 6. 返回标准化响应
		HotContentResponse response = new HotContentResponse();
		response.setHotContents(hotContents);
		return Collections.singletonList(response);
	}

	/**
	 * 获取文案排名变化
	 * @param contentIds 当前热门文案ID列表
	 * @return Map<文案ID, 排名变化>
	 */
	private Map<Long, Integer> getRankChanges(List<Long> contentIds) {
		// 从Redis获取上次排名（使用ZREVRANK）
		Map<Long, Integer> changeMap = new HashMap<>();

		contentIds.forEach(contentId -> {
			Long lastRank = redisTemplate.opsForZSet()
					.rank(RedisKeyUtil.getLastHotContentsKey(), contentId.toString());

			// 计算变化：当前排名 - 上次排名（null表示新上榜）
			int currentRank = contentIds.indexOf(contentId);
			if (lastRank != null) {
				changeMap.put(contentId, lastRank.intValue() - currentRank);
			} else {
				changeMap.put(contentId, null);
			}
		});

		return changeMap;
	}
}