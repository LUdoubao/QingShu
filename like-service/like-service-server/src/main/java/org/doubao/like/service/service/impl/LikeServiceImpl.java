package org.doubao.like.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.like.service.dto.LikeCountDTO;
import org.doubao.like.service.dto.request.BatchLikeStatusRequest;
import org.doubao.like.service.dto.request.ToggleLikeRequest;
import org.doubao.like.service.dto.response.*;
import org.doubao.like.service.entity.LikeCount;
import org.doubao.like.service.entity.LikeRecord;
import org.doubao.like.service.enums.EntityTypeEnum;
import org.doubao.like.service.enums.HotListType;
import org.doubao.like.service.enums.LikeAction;
import org.doubao.like.service.feign.QuoteClient;
import org.doubao.like.service.feign.UserClient;
import org.doubao.like.service.mapper.LikeCountMapper;
import org.doubao.like.service.mapper.LikeRecordMapper;
import org.doubao.like.service.messaging.LikeEventPublisher;
import org.doubao.like.service.service.HotContentRankManager;
import org.doubao.like.service.service.LikeService;
import org.doubao.like.service.utils.RedisKeyUtil;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.ResultCode;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.ratelimit.annotation.RateLimit;
import org.doubao.mall.common.ratelimit.enums.RateLimitDimension;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.mall.common.util.ConvertUtil;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.mall.common.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeService  {
	private static final Logger LOGGER = LoggerFactory.getLogger(LikeServiceImpl.class);
	@Resource
	private QuoteClient quoteServiceClient;
	@Resource
	private UserClient userClient;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private LikeRecordMapper likeRecordMapper;

	@Autowired
	private LikeCountMapper likeCountMapper;

	@Autowired
	private LikeEventPublisher likeEventPublisher;

	@Autowired
	private CommonTaskExecutor taskExecutor;

	@Autowired
	private HotContentRankManager hotContentRankManager;

	/**
	 * 点赞热度权重
	 */
	private static final long LIKE_HEAT_VALUE_WEIGHT = 100L;
	private static final int HOT_LIST_MAX_LIMIT = 100;
	private static final int HOT_LIST_MAX_WINDOW_HOURS = 720;
	private static final int HOT_LIST_CANDIDATE_MULTIPLIER = 5;
	@Override
	@Transactional
	@RateLimit(
			dimensions = {RateLimitDimension.USER, RateLimitDimension.IP},
			timeUnit = TimeUnit.MINUTES
	)
	public ToggleLikeResponse toggleLike(ToggleLikeRequest request) {
		Long userId = request.getUserId();
		Long operatorUserId = request.getOperatorUserId();

		String operatorUserName = Constants.DEFAULT_USER_NAME;
		String operatorUserAvatar = "";
		List<UserInfoDes> userInfos = userClient.getUsersByIds(Collections.singleton(operatorUserId)).getData();
		if (!CollectionUtils.isEmpty(userInfos)) {
			UserInfoDes userInfo = userInfos.get(0);
			operatorUserName = userInfo.getNickname();
			operatorUserAvatar = userInfo.getAvatarUrl();
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
						entityId,
						-1
				);
			}

			response.setAction(LikeAction.CANCEL.getName());
		} else {
			// 点赞
			redisTemplate.opsForValue().set(userLikeKey, true);
			redisTemplate.opsForValue().increment(countKey, 1L);

			// 如果是文案，更新热门集合
			if (request.getEntityType() == EntityTypeEnum.CONTENT.getType()) {
				redisTemplate.opsForZSet().incrementScore(
						RedisKeyUtil.getHotContentsKey(),
						entityId,
						1
				);
			}

			// 发送MQ消息通知文案所属用户
			likeEventPublisher.pushLikeNotification(userId, entityType, entityId,request.getContent(),
					operatorUserId, operatorUserName, operatorUserAvatar);

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
				String key = RedisKeyUtil.getEntityLikeCountKey(EntityTypeEnum.getNameByType(entityType), String.valueOf(id));
				redisTemplate.opsForValue().set(key, count, 1, TimeUnit.DAYS);
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
	public HotContentResponse getHotContents(String type, int page, int limit, Integer windowHours) {
		if (page <= 0) {
			throw new IllegalArgumentException("page must be greater than 0");
		}
		if (limit <= 0 || limit > HOT_LIST_MAX_LIMIT) {
			throw new IllegalArgumentException("limit must be between 1 and " + HOT_LIST_MAX_LIMIT);
		}
		HotListType hotListType = HotListType.fromCode(type);
		int resolvedWindowHours = resolveWindowHours(hotListType, windowHours);

		int candidateSize = Math.min(limit * HOT_LIST_CANDIDATE_MULTIPLIER, HOT_LIST_MAX_LIMIT);
		List<Long> candidateIds = hotContentRankManager.loadCandidateIds(hotListType, candidateSize);

		HotContentResponse response = new HotContentResponse();
		response.setPage(page);
		response.setLimit(limit);
		response.setType(hotListType.getCode());
		response.setWindowHours(resolvedWindowHours);
		response.setScoreSource(hotContentRankManager.getScoreSource(hotListType));
		response.setGeneratedAt(LocalDateTime.now());
		response.setRankMeta(buildRankMeta(hotListType, resolvedWindowHours));
		if (CollectionUtils.isEmpty(candidateIds)) {
			response.setTotal(0L);
			response.setHotContents(Collections.emptyList());
			return response;
		}

		Map<Long, Long> totalLikeMap = loadTotalLikeMap(candidateIds);
		Map<Long, Long> recentLikeMap = loadRecentLikeMap(candidateIds, resolvedWindowHours);

		List<Long> rankedIds = candidateIds.stream()
				.sorted(Comparator
						.comparingLong((Long contentId) -> calculateHeatValue(
								totalLikeMap.getOrDefault(contentId, 0L),
								recentLikeMap.getOrDefault(contentId, 0L)))
						.reversed()
						.thenComparing(Comparator.comparingLong((Long contentId) -> totalLikeMap.getOrDefault(contentId, 0L)).reversed())
						.thenComparingLong(Long::longValue))
				.collect(Collectors.toList());
		Map<Long, Integer> rankChangeMap = getRankChanges(hotListType, rankedIds);

		response.setTotal((long) rankedIds.size());
		int fromIndex = Math.min((page - 1) * limit, rankedIds.size());
		int toIndex = Math.min(fromIndex + limit, rankedIds.size());
		List<Long> pageIds = rankedIds.subList(fromIndex, toIndex);
		Map<Long, Map<String, Object>> quoteMap = fetchQuoteMap(pageIds);
		Map<Long, Integer> rankMap = new HashMap<>(rankedIds.size());
		for (int i = 0; i < rankedIds.size(); i++) {
			rankMap.put(rankedIds.get(i), i + 1);
		}

		List<HotContentResponse.HotContentItem> items = pageIds.stream()
				.map(contentId -> buildHotContentItem(contentId, totalLikeMap, recentLikeMap, rankChangeMap, rankMap, quoteMap))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		response.setHotContents(items);
		return response;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Page<LikeQuoteVo> likeList(Long userId, int page, int size) {
		if (DoubaoUtils.isEmpty(userId)) {
			// 获取当前登录用户ID
			userId = UserContext.getUserId();
		} else {
			// 鉴权TODO
		}
		Page<LikeRecord> pageParam = new Page<>(page, size);
		LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<LikeRecord>();
		queryWrapper.eq(LikeRecord::getUserId, userId)
				.eq(LikeRecord::getLiked, 1)
				.eq(LikeRecord::getEntityType, EntityTypeEnum.CONTENT.getType())
				.orderByDesc(LikeRecord::getCreatedTime);
		Page<LikeRecord> likes = likeRecordMapper.selectPage(pageParam, queryWrapper);
		Page<LikeQuoteVo> likeQuoteVoPage = new Page<>(page, size, likes.getTotal());
		if (likes.getRecords().isEmpty()) {
			return likeQuoteVoPage;
		}
		List<Long> quoteIds =  likes.getRecords().stream().map(LikeRecord::getEntityId).map(Long::parseLong).collect(Collectors.toList());
		List<Map<String, Object>> data = quoteServiceClient.getQuotesByIds(quoteIds).getData();
		List<LikeCountDTO> likeCountDTOS = likeRecordMapper.countByEntities(EntityTypeEnum.CONTENT.getType(), quoteIds);
		if (data != null && !data.isEmpty()) {
			List<LikeQuoteVo> delList = new ArrayList<>();
			List<LikeQuoteVo> collected = likes.getRecords().stream().map(like -> {
				LikeQuoteVo likeVo = new LikeQuoteVo();
				Optional<Map<String, Object>> first = data.stream().filter(d -> String.valueOf(d.get("id")).equals(like.getEntityId())).findFirst();
				boolean present = first.isPresent();
				if (present) {
					likeVo.setQuote(first.get());
				} else {
					delList.add(likeVo);
				}
				Optional<LikeCountDTO> countDTO = likeCountDTOS.stream().filter(d -> String.valueOf(d.getEntityId()).equals(like.getEntityId())).findFirst();
				likeVo.setLikeCount(countDTO.isPresent() ? countDTO.get().getCount() : 0L);
				return likeVo;
			}).collect(Collectors.toList());
			// 删除已失效的数据
			collected.removeAll(delList);
			likeQuoteVoPage.setRecords(collected);
		}
		return likeQuoteVoPage;
	}

	@Override
	public Map<Long, Long> batchCounts(List<Long> contentIds) {
		if (CollectionUtils.isEmpty(contentIds)) {
			return Collections.emptyMap();
		}

		Map<Long, Long> result = new HashMap<>(contentIds.size());
		List<String> quoteIds = contentIds.stream().map(Object::toString).collect(Collectors.toList());
		List<LikeCountVo> likeCountVos = likeRecordMapper.countLikesByEntityIds(quoteIds);

		for (LikeCountVo likeCountVo : likeCountVos) {
			result.put(Long.parseLong(likeCountVo.getQuoteId()), likeCountVo.getLikeCount());
		}
		return result;
	}

	@Override
	public Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			LOGGER.error("批量查询每日点赞数参数为空");
			return Collections.emptyMap();
		}

		// 1. 解析参数：获取文章ID列表和日期列表
		List<Long> contentIds = ConvertUtil.safeConvertToListOfLong(params.get("quoteIds"));
		List<LocalDate> dates = ConvertUtil.safeConvertToListOfLocalDate(params.get("dates"));


		// 参数校验：文章ID和日期列表不可为空
		if (CollectionUtils.isEmpty(contentIds) || CollectionUtils.isEmpty(dates)) {
			LOGGER.error("批量查询每日点赞数参数不完整：contentIds={}, dates={}", contentIds, dates);
			return Collections.emptyMap();
		}

		// 2. 转换文章ID为字符串（因为LikeRecord中entityId是String类型）
		List<String> entityIds = contentIds.stream()
				.map(String::valueOf)
				.collect(Collectors.toList());

		// 3. 调用Mapper查询指定日期和文章的有效点赞数总和（按日期分组）
		int entityType = EntityTypeEnum.CONTENT.getType();
		List<Map<String, Object>> dailyCounts = likeRecordMapper.selectDailyLikeCounts(
				entityIds, entityType, dates);

		// 4. 转换查询结果为Map<LocalDate, Long>（日期→当日总点赞数）
		Map<LocalDate, Long> resultMap = new HashMap<>(dates.size());

		// 先初始化所有日期的计数为0（确保每个日期都有返回值）
		for (LocalDate date : dates) {
			resultMap.put(date, 0L);
		}

		// 填充查询到的实际计数（覆盖初始值）
		for (Map<String, Object> countMap : dailyCounts) {
			// 从查询结果中提取日期和计数（数据库字段与Java类型映射）
			LocalDate statDate = ConvertUtil.safeParseLocalDate(countMap.get("stat_date"));
			Long totalCount = ConvertUtil.safeParseLong(countMap.get("total_count"));

			// 仅更新输入日期列表中存在的日期
			if (statDate != null && resultMap.containsKey(statDate)) {
				resultMap.put(statDate, totalCount);
			}
		}

		LOGGER.info("批量查询每日点赞数完成：日期范围={}至{}, 文章数量={}, 结果={}",
				dates.get(0), dates.get(dates.size() - 1), contentIds.size(), resultMap);
		return resultMap;
	}

	/**
	 * 获取文案排名变化
	 * @param contentIds 当前热门文案ID列表
	 * @return Map<文案ID, 排名变化>
	 */
	private Map<Long, Integer> getRankChanges(HotListType type, List<Long> contentIds) {
		Map<Long, Integer> changeMap = new HashMap<>();
		Map<Long, Integer> currentRankMap = new HashMap<>(contentIds.size());
		for (int i = 0; i < contentIds.size(); i++) {
			currentRankMap.put(contentIds.get(i), i);
		}

		contentIds.forEach(contentId -> {
			Long lastRank = redisTemplate.opsForZSet()
					.reverseRank(RedisKeyUtil.getLastHotContentsKey(type), contentId.toString());
			Integer currentRank = currentRankMap.get(contentId);
			if (lastRank != null && currentRank != null) {
				changeMap.put(contentId, lastRank.intValue() - currentRank);
			} else {
				changeMap.put(contentId, null);
			}
		});

		return changeMap;
	}

	private int resolveWindowHours(HotListType type, Integer windowHours) {
		int resolved = hotListTypeWindow(type, windowHours);
		if (resolved <= 0 || resolved > HOT_LIST_MAX_WINDOW_HOURS) {
			throw new IllegalArgumentException("windowHours must be between 1 and " + HOT_LIST_MAX_WINDOW_HOURS);
		}
		return resolved;
	}

	private int hotListTypeWindow(HotListType type, Integer windowHours) {
		if (type == HotListType.ALL) {
			return windowHours == null ? hotContentRankManager.getConfiguredWindowHours(type) : windowHours;
		}
		return hotContentRankManager.getConfiguredWindowHours(type);
	}

	private Map<Long, Long> loadTotalLikeMap(List<Long> contentIds) {
		if (CollectionUtils.isEmpty(contentIds)) {
			return Collections.emptyMap();
		}
		Map<Long, Long> result = new HashMap<>(contentIds.size());
		List<LikeCountDTO> totalCounts = likeRecordMapper.countByEntities(EntityTypeEnum.CONTENT.getType(), contentIds);
		for (LikeCountDTO dto : totalCounts) {
			result.put(dto.getEntityId(), dto.getCount());
		}
		return result;
	}

	private Map<Long, Long> loadRecentLikeMap(List<Long> contentIds, int windowHours) {
		if (CollectionUtils.isEmpty(contentIds)) {
			return Collections.emptyMap();
		}
		LocalDateTime startTime = LocalDateTime.now().minusHours(windowHours);
		Map<Long, Long> result = new HashMap<>(contentIds.size());
		List<LikeCountDTO> recentCounts = likeRecordMapper.countRecentLikesByEntities(
				EntityTypeEnum.CONTENT.getType(), contentIds, startTime);
		for (LikeCountDTO dto : recentCounts) {
			result.put(dto.getEntityId(), dto.getCount());
		}
		return result;
	}

	private Map<Long, Map<String, Object>> fetchQuoteMap(List<Long> contentIds) {
		if (CollectionUtils.isEmpty(contentIds)) {
			return Collections.emptyMap();
		}
		Result<List<Map<String, Object>>> quoteResult = quoteServiceClient.getQuotesByIds(contentIds);
		if (quoteResult == null || !Objects.equals(quoteResult.getCode(), ResultCode.SUCCESS.getCode())
				|| CollectionUtils.isEmpty(quoteResult.getData())) {
			LOGGER.error("failed to load hot quote details: {}", quoteResult);
			return Collections.emptyMap();
		}
		return quoteResult.getData().stream()
				.filter(Objects::nonNull)
				.filter(item -> item.get("id") != null)
				.collect(Collectors.toMap(item -> Long.parseLong(String.valueOf(item.get("id"))), item -> item));
	}

	private HotContentResponse.HotContentItem buildHotContentItem(Long contentId,
										 Map<Long, Long> totalLikeMap,
										 Map<Long, Long> recentLikeMap,
										 Map<Long, Integer> rankChangeMap,
										 Map<Long, Integer> rankMap,
										 Map<Long, Map<String, Object>> quoteMap) {
		Map<String, Object> quote = quoteMap.get(contentId);
		if (quote == null) {
			return null;
		}
		long totalLikes = totalLikeMap.getOrDefault(contentId, 0L);
		long recentLikes = recentLikeMap.getOrDefault(contentId, 0L);
		Integer rankChange = rankChangeMap.get(contentId);

		HotContentResponse.HotContentItem item = new HotContentResponse.HotContentItem();
		item.setContentId(contentId);
		item.setRank(rankMap.get(contentId));
		item.setLikeCount(totalLikes);
		item.setRecentLikeCount(recentLikes);
		item.setHeatValue(calculateHeatValue(totalLikes, recentLikes));
		item.setRankChange(rankChange);
		item.setTrend(resolveTrend(rankChange, recentLikes, totalLikes));
		item.setQuote(quote);
		return item;
	}

	private long calculateHeatValue(long totalLikes, long recentLikes) {
		double stableScore = Math.log1p(Math.max(totalLikes, 0L)) * 0.35D;
		double trendScore = Math.log1p(Math.max(recentLikes, 0L)) * 0.65D;
		return Math.round((stableScore + trendScore) * LIKE_HEAT_VALUE_WEIGHT * 100);
	}

	private String resolveTrend(Integer rankChange, long recentLikes, long totalLikes) {
		if (rankChange == null) {
			return "NEW";
		}
		if (rankChange > 0) {
			return "UP";
		}
		if (rankChange < 0) {
			return "DOWN";
		}
		if (recentLikes > 0 && recentLikes * 2 >= Math.max(totalLikes, 1L)) {
			return "HOT";
		}
		return "STABLE";
	}

	private HotContentResponse.RankMeta buildRankMeta(HotListType type, int windowHours) {
		HotContentResponse.RankMeta rankMeta = new HotContentResponse.RankMeta();
		rankMeta.setType(type.getCode());
		rankMeta.setWindowHours(windowHours);
		rankMeta.setScoreSource(hotContentRankManager.getScoreSource(type));
		rankMeta.setRefreshIntervalMinutes(hotContentRankManager.getRefreshIntervalMinutes());
		rankMeta.setRisingMinCurrentLikes(type == HotListType.RISING ? hotContentRankManager.getRisingMinCurrentLikes() : null);
		return rankMeta;
	}

}
