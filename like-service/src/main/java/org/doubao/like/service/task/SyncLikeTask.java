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
	@Scheduled(fixedRate = 2 * 60 * 1000)
	public void syncLikeCount() {
		LOGGER.info("开始同步点赞数据到数据库");

		// 扫描Redis中所有点赞计数key
		Set<String> keys = redisTemplate.keys(RedisKeyUtil.getEntityLikeCountKey("*", "*"));

		if (keys == null) {
			LOGGER.info("没有点赞数据需要同步");
			return;
		}
		for (String key : keys) {
			try {
				// 解析实体信息
				String[] parts = key.split(":");
				EntityTypeEnum entityType = EntityTypeEnum.valueOf(parts[1]);
				Long entityId = Long.parseLong(parts[2]);

				// 获取计数
				Object value = redisTemplate.opsForValue().get(key);
				if (value == null) continue;

				int count = Integer.parseInt(value.toString());

				// 更新数据库
				LikeCount likeCount = new LikeCount();
				likeCount.setEntityType(entityType.getType());
				likeCount.setEntityId(entityId);
				likeCount.setCount(count);
				likeCount.setUpdatedAt(LocalDateTime.now());

				likeCountMapper.updateSync(likeCount);

			} catch (Exception e) {
				LOGGER.error("同步计数失败 key: {}, error: {},time:{}", key, e.getMessage(), LocalDateTime.now());
			}
		}

		LOGGER.info("同步点赞数据完成,time:{}", LocalDateTime.now());
	}
}