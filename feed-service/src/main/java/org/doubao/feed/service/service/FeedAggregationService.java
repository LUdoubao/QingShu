package org.doubao.feed.service.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.doubao.feed.service.feign.QuoteClient;
import org.doubao.feed.service.feign.UserClient;
import org.doubao.feed.service.mapper.EventTimelineMapper;
import org.doubao.feed.service.mapper.UserTimelineMapper;
import org.doubao.feed.service.model.dto.ContentDTO;
import org.doubao.feed.service.model.dto.DynamicDTO;
import org.doubao.feed.service.model.entity.EventTimeline;
import org.doubao.feed.service.model.entity.UserTimeline;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedAggregationService {

	private static final Logger logger = LoggerFactory.getLogger(FeedAggregationService.class);

	@Resource
	private UserClient userClient;
	@Resource
	private QuoteClient quoteClient;
	@Resource
	private UserTimelineMapper userTimelineMapper;
	@Resource
	private EventTimelineMapper eventTimelineMapper;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	@Resource
	private SortingService sortingService;

	// 大V粉丝数阈值
	private static final long VIP_FOLLOWER_THRESHOLD = 10000;
	// Redis缓存键
	private static final String FOLLOWS_CACHE_KEY = "user:follows:";
	private static final String VIP_TIMELINE_CACHE_KEY = "vip:timeline:";

	/**
	 * 聚合用户关注的动态
	 */
	public PageResult<DynamicDTO> aggregateUserFeeds(Long userId, int page, int pageSize, int sortType) {
		// 1. 获取用户关注列表
		List<Long> followees = getUserFollowees(userId);
		logger.info("获取用户关注列表：{}", followees);
		if (followees.isEmpty()) {
			return new PageResult<>(page, pageSize, 0, Collections.emptyList());
		}

		// 2. 区分普通用户和大V
		Map<Boolean, List<Long>> followeeGroup = groupFolloweesByVip(followees);
		logger.info("用户列表：{}", followeeGroup);


		List<Long> normalFollowees = followeeGroup.getOrDefault(false, Collections.emptyList());
		logger.info("普通用户列表：{}", normalFollowees);
		List<Long> vipFollowees = followeeGroup.getOrDefault(true, Collections.emptyList());
		logger.info("大V用户列表：{}", vipFollowees);

		// 3. 获取普通用户动态
		List<UserTimeline> normalList = new ArrayList<>();
		if (!normalFollowees.isEmpty()) {
			normalList = userTimelineMapper.queryUserTimeline(
					userId, normalFollowees, (page - 1) * pageSize, pageSize);
		}


		// 4. 获取大V用户动态
		List<EventTimeline> vipList = new ArrayList<>();
		if (!vipFollowees.isEmpty()) {
			vipList = eventTimelineMapper.queryVipTimeline(
					vipFollowees, (page - 1) * pageSize, pageSize);
		}

		logger.info("普通用户动态：{}", normalList);
		logger.info("大V用户动态：{}", vipList);
		// 5. 合并动态并转换为DTO
		List<DynamicDTO> dynamics = mergeAndConvertDynamics(normalList, vipList);
		logger.info("聚合后的动态：{}", JSON.toJSONString(dynamics));

		// 6. 补充动态详情
		List<DynamicDTO> completeDynamics = completeDynamicDetails(dynamics);
		logger.info("补充动态详情：{}", completeDynamics);

		// 7. 排序

		List<DynamicDTO> sortedDynamics = new ArrayList<>();
		if (!completeDynamics.isEmpty()) {
			logger.info("执行排序");
			sortedDynamics = sortingService.sortDynamics(completeDynamics, sortType, userId);
		}
		logger.info("排序后的动态：{}", sortedDynamics);

		// 8. 计算总条数
		long countUserTimeline = userTimelineMapper.countUserTimeline(userId, normalFollowees);
		logger.info("普通用户动态数：{}", countUserTimeline);
		long countVipTimeline = 0L;
		if (!vipFollowees.isEmpty()) {
			countVipTimeline = eventTimelineMapper.countVipTimeline(vipFollowees);
		}
		logger.info("大V用户动态数：{}", countVipTimeline);
		long total = countUserTimeline + countVipTimeline;
		logger.info("总条数：{}", total);

		return new PageResult<>(page, pageSize, total, sortedDynamics);
	}

	/**
	 * 获取用户关注列表
	 */
	private List<Long> getUserFollowees(Long userId) {
		String cacheKey = FOLLOWS_CACHE_KEY + userId;

		// 1. 从缓存获取
		List<Object> cacheFollows = redisTemplate.opsForList().range(cacheKey, 0, -1);
		if (cacheFollows != null && !cacheFollows.isEmpty()) {
			return cacheFollows.stream()
					.map(followee -> Long.parseLong(followee.toString()))
					.collect(Collectors.toList());
		}

		// 2. 从用户服务获取
		List<Long> followees = userClient.getFollowees(userId).getData();
		if (!followees.isEmpty()) {
			// 3. 更新缓存，有效期1小时
			redisTemplate.opsForList().rightPushAll(cacheKey, followees.stream()
					.map(String::valueOf).toArray());
			redisTemplate.expire(cacheKey, 1, java.util.concurrent.TimeUnit.HOURS);
		}

		return followees;
	}

	/**
	 * 将关注者分为普通用户和大V
	 */
	private Map<Boolean, List<Long>> groupFolloweesByVip(List<Long> followees) {
		// 获取关注者的粉丝数
		Map<Long, Long> followerCountMap = userClient.getFollowerCounts(followees).getData();

		return followees.stream()
				.collect(Collectors.partitioningBy(
						followeeId -> {
							Long count = followerCountMap.getOrDefault(followeeId, 0L);
							return count > VIP_FOLLOWER_THRESHOLD;
						}
				));
	}

	/**
	 * 合并动态并转换为DTO
	 */
	private List<DynamicDTO> mergeAndConvertDynamics(
			List<UserTimeline> normalDynamics,
			List<EventTimeline> vipDynamics) {

		List<DynamicDTO> result = new ArrayList<>();

		// 转换普通用户动态
		if (!normalDynamics.isEmpty()) {
			result.addAll(normalDynamics.stream()
					.map(this::convertToDynamicDTO)
					.collect(Collectors.toList()));
		}

		// 转换大V动态
		if (!vipDynamics.isEmpty()) {
			result.addAll(vipDynamics.stream()
					.map(this::convertToDynamicDTO)
					.collect(Collectors.toList()));
		}

		return result;
	}

	/**
	 * 转换UserTimeline为DynamicDTO
	 */
	private DynamicDTO convertToDynamicDTO(UserTimeline timeline) {
		DynamicDTO dto = new DynamicDTO();
		dto.setId(timeline.getId());
		dto.setEventId(timeline.getEventId());
		dto.setActorId(timeline.getActorId());
		dto.setEventType(timeline.getEventType());
		dto.setTargetId(timeline.getTargetId());
		dto.setTargetType(timeline.getTargetType());
		dto.setEventTime(timeline.getEventTime());
		dto.setWeight(timeline.getWeight());
		return dto;
	}

	/**
	 * 转换EventTimeline为DynamicDTO
	 */
	private DynamicDTO convertToDynamicDTO(EventTimeline timeline) {
		DynamicDTO dto = new DynamicDTO();
		dto.setId(timeline.getId());
		dto.setEventId(timeline.getEventId());
		dto.setActorId(timeline.getActorId());
		dto.setEventType(timeline.getEventType());
		dto.setTargetId(timeline.getTargetId());
		dto.setTargetType(timeline.getTargetType());
		dto.setEventTime(timeline.getEventTime());
		dto.setWeight(timeline.getWeight());
		return dto;
	}

	/**
	 * 补充动态详情
	 */
	private List<DynamicDTO> completeDynamicDetails(List<DynamicDTO> dynamics) {
		if (dynamics.isEmpty()) {
			return dynamics;
		}

		// 1. 批量获取用户信息
		Set<Long> actorIds = dynamics.stream()
				.map(DynamicDTO::getActorId)
				.collect(Collectors.toSet());
		logger.info("用户ID actorIds列表：{}", actorIds);

		List<UserInfoDes> userInfos = userClient.getUsersByIds(actorIds).getData();
		logger.info("用户信息列表：{}", JSON.toJSONString(userInfos));
		Map<Long, UserInfoDes> userMap = userInfos.stream()
				.collect(Collectors.toMap(UserInfoDes::getId, user -> user));
		logger.info("用户Map：{}", JSON.toJSONString(userMap));

		// 2. 批量获取内容信息
		Map<String, List<Long>> targetMap = dynamics.stream()
				.collect(Collectors.groupingBy(
						DynamicDTO::getTargetType,
						Collectors.mapping(DynamicDTO::getTargetId, Collectors.toList())
				));
		logger.info("内容类型列表targetMap：{}", JSON.toJSONString(targetMap));

		Map<String, Map<Long, ContentDTO>> contentMap = new HashMap<>();
		targetMap.forEach((type, ids) -> {
			if (type != null && type.equals("quote")) {
				List<Map<String, Object>> data = quoteClient.batch(ids).getData();
				logger.info("内容列表data：{}", JSON.toJSONString(data));
				Map<Long, ContentDTO> typeContentMap = data.stream()
						.map(item -> {
							logger.info("item: {}", JSON.toJSONString(item));
							ContentDTO content = new ContentDTO();
							content.setId(item.get("id") != null ? Long.parseLong(item.get("id").toString()) : 0L);
							content.setTitle(item.get("title") != null ? item.get("title").toString() : "");
							content.setContent(item.get("content") != null ? item.get("content").toString() : "");
							content.setUrl(item.get("url") != null ? item.get("url").toString() : "");
							content.setCoverImage(item.get("coverImage") != null ? item.get("coverImage").toString() : "");
							content.setCreatedId(item.get("createdId") != null ? Long.parseLong(item.get("createdId").toString()) : 0L);
							logger.info("content: {}", JSON.toJSONString(content));
							return content;
						})
						.collect(Collectors.toMap(ContentDTO::getId, content -> content));
				contentMap.put(type, typeContentMap);
			}
		});

		logger.info("内容contentMap：{}", JSON.toJSONString(contentMap));
		// 4. 补充信息到DTO
		List<DynamicDTO> delList = new ArrayList<>();
		dynamics.forEach(dynamic -> {
			// 补充用户信息
			UserInfoDes user = userMap.get(dynamic.getActorId());
			if (user != null) {
				dynamic.setActorName(user.getNickname());
				dynamic.setActorAvatar(user.getAvatarUrl());
			}

			// 补充内容信息
			Map<Long, ContentDTO> typeContentMap = contentMap.get(dynamic.getTargetType());
			logger.info("内容typeContentMap：{}", JSON.toJSONString(typeContentMap));
			if (typeContentMap != null) {
				ContentDTO content = typeContentMap.get(dynamic.getTargetId());
				if (content != null) {
					dynamic.setContentSummary(getContentSummary(content.getContent()));
					dynamic.setContentUrl(content.getUrl());
				} else {
					// 删除无效动态
					delList.add(dynamic);
				}
			}

			// 补充事件类型名称
			dynamic.setEventTypeName(getEventTypeName(dynamic.getEventType()));
		});

		// 删除无效动态
		dynamics.removeAll(delList);

		return dynamics;
	}

	/**
	 * 获取内容摘要
	 */
	private String getContentSummary(String content) {
		if (content == null || content.length() <= 20) {
			return content;
		}
		return content.substring(0, 20) + "...";
	}

	/**
	 * 获取事件类型名称
	 */
	private String getEventTypeName(Integer eventType) {
		switch (eventType) {
			case 1: return "发布了新动态";
			case 2: return "的动态上了榜单";
			case 3: return "删除了一条动态";
			case 4: return "赞了你的动态";
			case 5: return "取消了点赞";
			default: return "有新动态";
		}
	}
}

