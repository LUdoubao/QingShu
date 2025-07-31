package org.doubao.like.service.task;

import lombok.extern.slf4j.Slf4j;
import org.doubao.like.service.entity.LikeCount;
import org.doubao.like.service.enums.EntityTypeEnum;
import org.doubao.like.service.mapper.LikeCountMapper;
import org.doubao.like.service.utils.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// SyncLikeTask.java
@Component
public class SyncLikeTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(SyncLikeTask.class);
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private LikeCountMapper likeCountMapper;

	// 每5分钟同步一次
	@Scheduled(fixedRate =  60* 60 * 1000)
	public void syncLikeCount() {
		LOGGER.info("开始同步点赞数据到数据库");

		// 扫描Redis中所有点赞计数key
		Set<String> keys = redisTemplate.keys(RedisKeyUtil.getEntityLikeCountKey("*", "*"));

		if (keys == null || keys.isEmpty()) {
			LOGGER.info("没有点赞数据需要同步");
			return;
		}

		for (String key : keys) {
			LOGGER.info("key: {}", key);
		}
		batchSyncLikeCounts(keys);

		LOGGER.info("同步点赞数据完成,time:{}", LocalDateTime.now());
	}

	public void batchSyncLikeCounts(Set<String> keys) {
		List<LikeCount> updateList = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();

		for (String key : keys) {
			try {
				// 解析实体信息
				String[] parts = key.split(":");
				EntityTypeEnum entityType = EntityTypeEnum.getByName(parts[1]);
				Long entityId = Long.parseLong(parts[2]);

				// 获取计数
				Object value = redisTemplate.opsForValue().get(key);
				if (value == null) continue;

				int count = Integer.parseInt(value.toString());

				// 构建更新对象
				LikeCount likeCount = new LikeCount();
				assert entityType != null;
				likeCount.setEntityType(entityType.getType());
				likeCount.setEntityId(entityId);
				likeCount.setCount(count);
				likeCount.setUpdatedAt(now);

				updateList.add(likeCount);
			} catch (Exception e) {
				LOGGER.error("同步计数异常 key: {}, error: {}", key, e.getMessage());
			}
		}

		// 批量更新
		if (!updateList.isEmpty()) {
			try {
				int batchSize = 500; // 每批处理500条
				int total = updateList.size();

				for (int i = 0; i < total; i += batchSize) {
					int end = Math.min(i + batchSize, total);
					List<LikeCount> subList = updateList.subList(i, end);

					likeCountMapper.batchUpdateCounts(subList);
					LOGGER.info("已批量更新点赞计数: {}/{}", end, total);
				}
			} catch (Exception e) {
				LOGGER.error("批量更新点赞计数失败", e);
			}
		}
	}
}