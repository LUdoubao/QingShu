package org.doubao.view.count.service.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.doubao.view.count.service.entity.ContentView;
import org.doubao.view.count.service.entity.UserViewLog;
import org.doubao.view.count.service.entity.ViewCorrectionLog;
import org.doubao.view.count.service.mapper.ContentViewMapper;
import org.doubao.view.count.service.service.UserViewLogService;
import org.doubao.view.count.service.service.ViewCorrectionLogService;
import org.doubao.view.count.service.service.ViewCountService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ViewCountTask {
	// 分布式锁前缀
	private static final String LOCK_PREFIX = "view:lock:";
	private static final String VIEW_COUNT_PREFIX = "view:count:";
	// 缓存过期时间随机偏移量（防雪崩）
	private static final int RANDOM_EXPIRE_OFFSET = 600;
	@Autowired
	private RedissonClient redissonClient;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	private static final Logger log = LoggerFactory.getLogger(ViewCountTask.class);
	@Autowired
	private ContentViewMapper contentViewMapper;
	@Resource
	private ViewCountService viewCountService;
	@Autowired
	private UserViewLogService userViewLogService;
	// 校正通知阈值（百分比）
	@Value("${view-count.correction-notify-threshold:10}")
	private int correctionNotifyThreshold;
	@Autowired
	private ViewCorrectionLogService viewCorrectionLogService;



	@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
	@Retryable(maxAttempts = 3) // 失败重试3次
	public void cleanAndCorrectData() {
		// 分布式锁：防止多实例重复执行定时任务
		String lockKey = LOCK_PREFIX + "clean:correct";
		RLock lock = redissonClient.getLock(lockKey);
		try {
			// 尝试获取锁（10秒等待，30分钟自动释放）
			if (lock.tryLock(10, 1800, TimeUnit.SECONDS)) {
				log.info("开始执行数据清洗与校正任务");

				// 1. 批量同步Redis数据到数据库（优化为批量操作）
				syncAllRedisDataToDb();

				// 2. 识别并清理无效数据
				cleanInvalidViews();

				log.info("数据清洗与校正任务执行完成");
			} else {
				log.info("数据清洗与校正任务已被其他实例执行，跳过");
			}
		} catch (InterruptedException e) {
			log.error("获取数据清洗锁失败", e);
			Thread.currentThread().interrupt();
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
	// 批量同步Redis数据到数据库
	private void syncAllRedisDataToDb() {
		log.info("开始批量同步Redis数据到数据库");
		try {
			// 获取所有浏览量相关的Redis键（分批获取，避免一次性获取过多）
			Set<String> keys = redisTemplate.keys(VIEW_COUNT_PREFIX + "*");
			if (keys == null || keys.isEmpty()) {
				log.info("没有需要同步的Redis浏览量数据");
				return;
			}

			// 分批处理（每批1000个）
			List<List<String>> batchKeys = splitBatch(new ArrayList<>(keys), 1000);
			int syncedCount = 0;

			for (List<String> batch : batchKeys) {
				// 批量获取Redis中的值
				List<Object> countObjs = redisTemplate.opsForValue().multiGet(batch);
				List<ContentView> insertList = new ArrayList<>();
				List<ContentView> updateList = new ArrayList<>();

				for (int i = 0; i < batch.size(); i++) {
					String key = batch.get(i);
					Object countObj = countObjs.get(i);

					if (countObj == null) {
						continue; // 跳过空值
					}

					Long contentId;
					Long redisCount;
					try {
						contentId = Long.parseLong(key.replace(VIEW_COUNT_PREFIX, ""));
						redisCount = Long.parseLong(countObj.toString());
					} catch (NumberFormatException e) {
						log.warn("Redis中浏览量数据格式错误，跳过同步: key={}, value={}", key, countObj);
						continue;
					}

					// 查询数据库中的记录
					LambdaQueryWrapper<ContentView> queryWrapper = new LambdaQueryWrapper<>();
					queryWrapper.eq(ContentView::getContentId, contentId);
					ContentView contentView = contentViewMapper.selectOne(queryWrapper);

					if (contentView == null) {
						// 构建新增记录
						ContentView newView = new ContentView();
						newView.setContentId(contentId);
						newView.setViewCount(redisCount);
						newView.setTodayCount(0);
						newView.setYesterdayCount(0);
						newView.setCreatedTime(LocalDateTime.now());
						newView.setUpdatedTime(LocalDateTime.now());
						insertList.add(newView);
					} else {
						// Redis计数大于数据库才更新
						if (redisCount > contentView.getViewCount()) {
							contentView.setViewCount(redisCount);
							contentView.setUpdatedTime(LocalDateTime.now());
							updateList.add(contentView);
							log.info("待同步浏览量: contentId={}, oldCount={}, newCount={}",
									contentId, contentView.getViewCount(), redisCount);
						}
					}
				}

				// 批量插入/更新数据库
				if (!insertList.isEmpty()) {
					viewCountService.saveBatch(insertList);
					syncedCount += insertList.size();
				}
				if (!updateList.isEmpty()) {
					viewCountService.updateBatchById(updateList);
					syncedCount += updateList.size();
				}
			}

			log.info("批量同步Redis数据到数据库完成，共处理 {} 个键", syncedCount);
		} catch (Exception e) {
			log.error("批量同步Redis数据到数据库失败", e);
		}
	}

	// 清理无效数据
	private void cleanInvalidViews() {
		// 1. 查询需要清理的无效浏览记录
		LambdaQueryWrapper<UserViewLog> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(UserViewLog::getIsValid, 0)
				.lt(UserViewLog::getViewTime, LocalDateTime.now().minusDays(1));
		List<UserViewLog> invalidLogs = userViewLogService.list(wrapper);

		if (CollectionUtils.isEmpty(invalidLogs)) {
			log.info("没有需要清理的无效浏览记录");
			return;
		}

		// 2. 按contentId分组统计需要减去的浏览量
		Map<Long, Long> correctionMap = invalidLogs.stream()
				.collect(Collectors.groupingBy(
						UserViewLog::getContentId,
						Collectors.counting()
				));

		// 3. 批量校正内容的浏览量
		List<ContentView> updateList = new ArrayList<>();
		Map<String, Long> redisUpdateMap = new HashMap<>();
		List<ViewCorrectionLog> correctionLogs = new ArrayList<>();

		for (Map.Entry<Long, Long> entry : correctionMap.entrySet()) {
			Long contentId = entry.getKey();
			Long reduceCount = entry.getValue();

			// 查询数据库记录
			ContentView contentView = contentViewMapper.selectOne(
					new QueryWrapper<ContentView>().eq("content_id", contentId));

			if (contentView == null) {
				continue;
			}

			Long beforeCount = contentView.getViewCount();
			Long afterCount = Math.max(0, beforeCount - reduceCount);

			// 计算变化百分比
			int changePercent = beforeCount == 0 ? 0 : (int) ((beforeCount - afterCount) * 100 / beforeCount);

			// 更新内容浏览量（移除version更新）
			contentView.setViewCount(afterCount);
			contentView.setUpdatedTime(LocalDateTime.now());
			updateList.add(contentView);

			// 记录Redis更新数据（批量更新）
			String countKey = VIEW_COUNT_PREFIX + contentId;
			redisUpdateMap.put(countKey, afterCount);
			// 重新设置过期时间（随机偏移）
			long expireTime = 24 * 3600 + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
			redisTemplate.expire(countKey, expireTime, TimeUnit.SECONDS);

			// 构建校正日志
			ViewCorrectionLog correctionLog = new ViewCorrectionLog();
			correctionLog.setContentId(contentId);
			correctionLog.setBeforeCount(beforeCount);
			correctionLog.setAfterCount(afterCount);
			correctionLog.setCorrectionTime(LocalDateTime.now());
			correctionLog.setReason("清理无效浏览记录");
			correctionLog.setOperator("system");
			correctionLogs.add(correctionLog);

			log.info("校正浏览量: contentId={}, before={}, after={}, reduce={}",
					contentId, beforeCount, afterCount, reduceCount);

			// 如果变化超过阈值，发送通知
			if (changePercent >= correctionNotifyThreshold) {
				sendCorrectionNotification(contentId, beforeCount, afterCount, changePercent);
			}
		}

		// 批量更新数据库
		if (!updateList.isEmpty()) {
			viewCountService.updateBatchById(updateList);
		}
		// 批量插入校正日志
		if (!correctionLogs.isEmpty()) {
			viewCorrectionLogService.saveBatch(correctionLogs);
		}
		// 批量更新Redis
		if (!redisUpdateMap.isEmpty()) {
			redisTemplate.opsForValue().multiSet(redisUpdateMap);
		}

		// 4. 批量删除无效日志
		List<Long> logIds = invalidLogs.stream()
				.map(UserViewLog::getId)
				.collect(Collectors.toList());
		// 分批删除（避免SQL过长）
		List<List<Long>> batchIds = splitBatch(logIds, 1000);
		for (List<Long> batch : batchIds) {
			userViewLogService.removeByIds(batch);
		}

		log.info("清理无效浏览记录: {} 条", logIds.size());
	}

	// 集合分批工具方法
	private <T> List<List<T>> splitBatch(List<T> list, int batchSize) {
		List<List<T>> batches = new ArrayList<>();
		for (int i = 0; i < list.size(); i += batchSize) {
			int end = Math.min(i + batchSize, list.size());
			batches.add(list.subList(i, end));
		}
		return batches;
	}

	// 发送校正通知
	private void sendCorrectionNotification(Long contentId, Long beforeCount, Long afterCount, int changePercent) {
		Long authorId = getAuthorIdByContentId(contentId);

		if (authorId == null) {
			log.warn("无法获取内容作者信息: contentId={}", contentId);
			return;
		}

		// 构建通知消息（示例，需根据实际MQ配置调整）
        /*
        ViewCorrectionNotificationDTO notification = new ViewCorrectionNotificationDTO();
        notification.setUserId(authorId);
        notification.setContentId(contentId);
        notification.setBeforeCount(beforeCount);
        notification.setAfterCount(afterCount);
        notification.setChangePercent(changePercent);
        notification.setNotifyTime(LocalDateTime.now());
        notification.setTitle("内容浏览量校正通知");
        notification.setContent(String.format(
                "您的内容（ID: %d）浏览量因系统清理无效数据已校正，从 %d 调整为 %d，变动率为 %d%%。",
                contentId, beforeCount, afterCount, changePercent));

        // 发送到消息队列
        rabbitTemplate.convertAndSend(
                "view.correction.notify.exchange",
                "view.correction.notify.key",
                notification);
        */

		log.info("发送浏览量校正通知: userId={}, contentId={}, changePercent={}%",
				authorId, contentId, changePercent);
	}
	// 获取内容作者ID（实际应通过Feign调用content-service）
	private Long getAuthorIdByContentId(Long contentId) {
		// 示例返回null，实际需替换为Feign调用
		return null;
	}
}
