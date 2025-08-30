package org.doubao.feed.service.service;

import org.doubao.feed.service.mapper.FeedFilterMapper;
import org.doubao.feed.service.model.dto.DynamicDTO;
import org.doubao.feed.service.model.dto.FilterSettingDTO;
import org.doubao.feed.service.model.entity.FeedFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FeedFilterService {

	private static final Logger log = LoggerFactory.getLogger(FeedFilterService.class);
	@Resource
	private FeedFilterMapper feedFilterMapper;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	// Redis缓存键前缀
	private static final String FILTER_CACHE_KEY = "feed:filter:";
	// 缓存过期时间（小时）
	private static final long CACHE_EXPIRE_HOURS = 1;

	/**
	 * 应用过滤规则
	 */
	public List<DynamicDTO> applyFilter(Long userId, List<DynamicDTO> dynamics) {
		if (dynamics.isEmpty()) {
			return dynamics;
		}

		// 1. 应用系统默认过滤
		List<DynamicDTO> filtered = applySystemFilter(dynamics);

		// 2. 获取用户自定义过滤设置
		FilterSettingDTO filterSetting = getFilterSetting(userId);
		if (filterSetting == null) {
			return filtered;
		}

		// 3. 应用用户自定义过滤
		return applyUserFilter(filtered, filterSetting);
	}

	/**
	 * 系统默认过滤
	 */
	private List<DynamicDTO> applySystemFilter(List<DynamicDTO> dynamics) {
		return dynamics.stream()
				// 过滤系统自动行为的动态（事件类型6及以上）
				.filter(dynamic -> dynamic.getEventType() < 6)
				// 合并重复的点赞动态
				.collect(Collectors.collectingAndThen(
						Collectors.toList(),
						this::mergeDuplicateLikeEvents
				));
	}

	/**
	 * 合并重复的点赞动态（同一用户1小时内对同一创作者的多条点赞）
	 */
	private List<DynamicDTO> mergeDuplicateLikeEvents(List<DynamicDTO> dynamics) {
		return dynamics.stream()
				.collect(Collectors.groupingBy(
						dynamic -> dynamic.getActorId() + ":" + dynamic.getEventType(),
						Collectors.mapping(DynamicDTO::getId, Collectors.toList())
				))
				.values().stream()
				.map(longs -> {
					return dynamics.stream()
							.filter(d -> d.getId().equals(longs.get(0)))
							.findFirst()
							.orElse(null);
				})
				.collect(Collectors.toList());
	}

	/**
	 * 应用用户自定义过滤
	 */
	private List<DynamicDTO> applyUserFilter(List<DynamicDTO> dynamics, FilterSettingDTO setting) {
		return dynamics.stream()
				// 过滤用户屏蔽的事件类型
				.filter(d -> !setting.getBlockedTypes().contains(d.getEventType()))
				// 过滤用户屏蔽的创作者
				.filter(d -> !setting.getBlockedActors().contains(d.getActorId()))
				.collect(Collectors.toList());
	}

	/**
	 * 获取用户的过滤设置
	 */
	public FilterSettingDTO getFilterSetting(Long userId) {
		String cacheKey = FILTER_CACHE_KEY + userId;

		// 1. 从缓存获取
		FilterSettingDTO cacheSetting = (FilterSettingDTO) redisTemplate.opsForValue().get(cacheKey);
		if (cacheSetting != null) {
			return cacheSetting;
		}

		// 2. 从数据库获取
		FeedFilter feedFilter = feedFilterMapper.findByUserId(userId);
		if (feedFilter == null) {
			return null;
		}

		// 3. 转换为DTO
		FilterSettingDTO settingDTO = new FilterSettingDTO();
		settingDTO.setBlockedTypes(feedFilter.getBlockedTypesList());
		settingDTO.setBlockedActors(feedFilter.getBlockedActorsList());

		// 4. 更新缓存
		redisTemplate.opsForValue().set(cacheKey, settingDTO, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

		return settingDTO;
	}

	/**
	 * 保存用户过滤设置
	 */
	@Transactional(rollbackFor = Exception.class)
	public void saveFilterSetting(Long userId, FilterSettingDTO settingDTO) {
		// 1. 查询或创建过滤设置记录
		FeedFilter feedFilter = feedFilterMapper.findByUserId(userId);
		if (feedFilter == null) {
			feedFilter = new FeedFilter();
			feedFilter.setUserId(userId);
		}

		// 2. 设置过滤规则
		feedFilter.setBlockedTypesList(settingDTO.getBlockedTypes());
		feedFilter.setBlockedActorsList(settingDTO.getBlockedActors());

		// 3. 保存到数据库
		if (feedFilter.getId() == null) {
			feedFilterMapper.insert(feedFilter);
		} else {
			feedFilterMapper.updateById(feedFilter);
		}

		// 4. 更新缓存
		String cacheKey = FILTER_CACHE_KEY + userId;
		redisTemplate.opsForValue().set(cacheKey, settingDTO, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
	}
}
