package org.doubao.like.service.service;

import org.doubao.like.service.config.HotListProperties;
import org.doubao.like.service.dto.LikeCountDTO;
import org.doubao.like.service.enums.EntityTypeEnum;
import org.doubao.like.service.enums.HotListType;
import org.doubao.like.service.mapper.LikeRecordMapper;
import org.doubao.like.service.utils.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class HotContentRankManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(HotContentRankManager.class);

	@Resource
	private LikeRecordMapper likeRecordMapper;

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Resource
	private HotListProperties hotListProperties;

	public boolean isEnabled() {
		return hotListProperties.isEnabled();
	}

	public int getConfiguredWindowHours(HotListType type) {
		switch (type) {
			case DAILY:
				return hotListProperties.getDailyWindowHours();
			case WEEKLY:
				return hotListProperties.getWeeklyWindowHours();
			case MONTHLY:
				return hotListProperties.getMonthlyWindowHours();
			case RISING:
				return hotListProperties.getRisingWindowHours();
			case ALL:
			default:
				return hotListProperties.getAllDefaultWindowHours();
		}
	}

	public int getRefreshLimit() {
		return hotListProperties.getRefreshLimit();
	}

	public int getRefreshIntervalMinutes() {
		return hotListProperties.getRefreshIntervalMinutes();
	}

	public long getRisingMinCurrentLikes() {
		return hotListProperties.getRisingMinCurrentLikes();
	}

	public String getScoreSource(HotListType type) {
		switch (type) {
			case DAILY:
				return "recent_likes_24h";
			case WEEKLY:
				return "recent_likes_7d";
			case MONTHLY:
				return "recent_likes_30d";
			case RISING:
				return "rising_score";
			case ALL:
			default:
				return "blended_heat";
		}
	}

	public boolean shouldPreloadOnStartup() {
		return hotListProperties.isPreloadOnStartup();
	}

	public void refreshAllRanks() {
		if (!isEnabled()) {
			LOGGER.info("hot list refresh skipped because feature is disabled");
			return;
		}
		for (HotListType type : HotListType.values()) {
			refreshRank(type);
		}
	}

	public void refreshRank(HotListType type) {
		try {
			Map<Long, Double> scoreMap = buildRankScores(type, getRefreshLimit());
			String currentKey = RedisKeyUtil.getHotContentsKey(type);
			String lastKey = RedisKeyUtil.getLastHotContentsKey(type);
			rotateSnapshot(currentKey, lastKey);
			redisTemplate.delete(currentKey);

			if (scoreMap.isEmpty()) {
				LOGGER.info("skip empty hot rank refresh, type={}", type.getCode());
				return;
			}

			Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
			scoreMap.entrySet().stream()
					.sorted(Map.Entry.<Long, Double>comparingByValue().reversed()
							.thenComparing(Map.Entry.comparingByKey()))
					.forEach(entry -> tuples.add(ZSetOperations.TypedTuple.of(
							String.valueOf(entry.getKey()), entry.getValue())));

			if (!tuples.isEmpty()) {
				redisTemplate.opsForZSet().add(currentKey, tuples);
			}
			LOGGER.info("refresh hot rank success, type={}, size={}", type.getCode(), tuples.size());
		} catch (Exception e) {
			LOGGER.error("refresh hot rank failed, type={}", type.getCode(), e);
		}
	}

	public List<Long> loadCandidateIds(HotListType type, int candidateSize) {
		Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
				.reverseRangeWithScores(RedisKeyUtil.getHotContentsKey(type), 0, candidateSize - 1);
		if (!CollectionUtils.isEmpty(tuples)) {
			return tuples.stream()
					.map(ZSetOperations.TypedTuple::getValue)
					.filter(Objects::nonNull)
					.map(String::valueOf)
					.map(Long::parseLong)
					.collect(Collectors.toList());
		}

		if (type == HotListType.RISING) {
			return new ArrayList<>(buildRankScores(type, candidateSize).keySet());
		}

		LocalDateTime now = LocalDateTime.now();
		List<LikeCountDTO> rows;
		if (type == HotListType.ALL) {
			rows = likeRecordMapper.selectTopLikedContents(EntityTypeEnum.CONTENT.getType(), candidateSize);
		} else {
			rows = likeRecordMapper.selectTopLikedContentsSince(
					EntityTypeEnum.CONTENT.getType(),
					now.minusHours(getConfiguredWindowHours(type)),
					candidateSize);
		}
		return rows.stream().map(LikeCountDTO::getEntityId).collect(Collectors.toList());
	}

	private Map<Long, Double> buildRankScores(HotListType type, int limit) {
		switch (type) {
			case DAILY:
			case WEEKLY:
			case MONTHLY:
				return loadWindowScores(getConfiguredWindowHours(type), limit);
			case RISING:
				return loadRisingScores(limit);
			case ALL:
			default:
				return loadAllScores(limit);
		}
	}

	private Map<Long, Double> loadAllScores(int limit) {
		return likeRecordMapper.selectTopLikedContents(EntityTypeEnum.CONTENT.getType(), limit)
				.stream()
				.collect(Collectors.toMap(LikeCountDTO::getEntityId, dto -> dto.getCount().doubleValue(),
						(a, b) -> a, LinkedHashMap::new));
	}

	private Map<Long, Double> loadWindowScores(int windowHours, int limit) {
		LocalDateTime now = LocalDateTime.now();
		return likeRecordMapper.selectTopLikedContentsSince(
						EntityTypeEnum.CONTENT.getType(), now.minusHours(windowHours), limit)
				.stream()
				.collect(Collectors.toMap(LikeCountDTO::getEntityId, dto -> dto.getCount().doubleValue(),
						(a, b) -> a, LinkedHashMap::new));
	}

	private Map<Long, Double> loadRisingScores(int limit) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime currentStart = now.minusHours(hotListProperties.getRisingWindowHours());
		LocalDateTime previousStart = currentStart.minusHours(hotListProperties.getRisingPreviousWindowHours());

		List<LikeCountDTO> currentTop = likeRecordMapper.selectTopLikedContentsBetween(
				EntityTypeEnum.CONTENT.getType(), currentStart, now, limit);
		List<LikeCountDTO> previousTop = likeRecordMapper.selectTopLikedContentsBetween(
				EntityTypeEnum.CONTENT.getType(), previousStart, currentStart, limit);

		LinkedHashSet<Long> candidateIds = new LinkedHashSet<>();
		currentTop.forEach(item -> candidateIds.add(item.getEntityId()));
		previousTop.forEach(item -> candidateIds.add(item.getEntityId()));
		if (candidateIds.isEmpty()) {
			return Collections.emptyMap();
		}

		List<Long> ids = new ArrayList<>(candidateIds);
		Map<Long, Long> currentMap = toCountMap(likeRecordMapper.countLikesByEntitiesBetween(
				EntityTypeEnum.CONTENT.getType(), ids, currentStart, now));
		Map<Long, Long> previousMap = toCountMap(likeRecordMapper.countLikesByEntitiesBetween(
				EntityTypeEnum.CONTENT.getType(), ids, previousStart, currentStart));

		return ids.stream()
				.filter(id -> currentMap.getOrDefault(id, 0L) >= hotListProperties.getRisingMinCurrentLikes())
				.collect(Collectors.toMap(id -> id,
						id -> calculateRisingScore(currentMap.getOrDefault(id, 0L), previousMap.getOrDefault(id, 0L)),
						(a, b) -> a, LinkedHashMap::new))
				.entrySet().stream()
				.sorted(Map.Entry.<Long, Double>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
				.limit(limit)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
	}

	private Map<Long, Long> toCountMap(List<LikeCountDTO> rows) {
		if (CollectionUtils.isEmpty(rows)) {
			return Collections.emptyMap();
		}
		return rows.stream().collect(Collectors.toMap(LikeCountDTO::getEntityId, LikeCountDTO::getCount));
	}

	private double calculateRisingScore(long currentCount, long previousCount) {
		long growth = Math.max(currentCount - previousCount, 0L);
		double ratio = previousCount <= 0 ? currentCount : (double) currentCount / previousCount;
		return currentCount * 100D + growth * 30D + ratio * 10D;
	}

	private void rotateSnapshot(String currentKey, String lastKey) {
		redisTemplate.delete(lastKey);
		Set<ZSetOperations.TypedTuple<Object>> current = redisTemplate.opsForZSet()
				.reverseRangeWithScores(currentKey, 0, -1);
		if (!CollectionUtils.isEmpty(current)) {
			redisTemplate.opsForZSet().add(lastKey, current);
		}
	}
}
