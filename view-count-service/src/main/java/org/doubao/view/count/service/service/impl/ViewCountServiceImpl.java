package org.doubao.view.count.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.doubao.mall.common.util.ConvertUtil;
import org.doubao.view.count.service.dto.ViewRecordDTO;
import org.doubao.view.count.service.entity.ContentView;
import org.doubao.view.count.service.entity.UserViewLog;
import org.doubao.view.count.service.entity.ViewCorrectionLog;
import org.doubao.view.count.service.mapper.ContentViewMapper;
import org.doubao.view.count.service.mapper.UserViewLogMapper;
import org.doubao.view.count.service.mapper.ViewCorrectionLogMapper;
import org.doubao.view.count.service.service.ViewCountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ViewCountServiceImpl extends ServiceImpl<ContentViewMapper, ContentView> implements ViewCountService {

	private static final Logger log = LoggerFactory.getLogger(ViewCountServiceImpl.class);
	@Autowired
	private ContentViewMapper contentViewMapper;

	@Autowired
	private UserViewLogMapper userViewLogMapper;

	@Autowired
	private ViewCorrectionLogMapper correctionLogMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${view-count.valid-duration}")
	private int validDuration;

	@Value("${view-count.list-valid-duration}")
	private int listValidDuration;

	@Value("${view-count.duplicate-time-window}")
	private int duplicateTimeWindow;

	@Value("${view-count.hot-content-threshold}")
	private int hotContentThreshold;

	@Value("${view-count.ip-limit-count}")
	private int ipLimitCount;

	@Value("${view-count.ip-limit-period}")
	private int ipLimitPeriod;

	@Value("${view-count.correction-notify-threshold}")
	private int correctionNotifyThreshold;

	// Redis key 前缀
	private static final String VIEW_COUNT_PREFIX = "view:count:";
	private static final String USER_VIEW_PREFIX = "view:user:";
	private static final String VIEW_TREND_PREFIX = "view:trend:";
	private static final String IP_LIMIT_PREFIX = "view:ip:limit:";
	private static final String HOT_CONTENT_PREFIX = "view:hot:";

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean recordView(ViewRecordDTO record) {
		// 1. 验证参数
		if (record.getContentId() == null || StringUtils.isEmpty(record.getUserIdentity())) {
			log.error("无效的浏览记录参数: {}", record);
			return false;
		}

		// 2. 检查是否为作者本人浏览
		if (Boolean.TRUE.equals(record.getAuthor())) {
			log.info("作者本人浏览，不计数: contentId={}, userIdentity={}",
					record.getContentId(), record.getUserIdentity());
			return false;
		}

		// 3. IP限流检查
		if (!checkIpLimit(record.getIpAddress())) {
			log.warn("IP访问频率超限: ip={}, contentId={}",
					record.getIpAddress(), record.getContentId());
			return false;
		}

		// 4. 检查是否为有效浏览
		boolean isValid = isValidView(record);
		if (!isValid) {
			log.info("无效浏览: contentId={}, userIdentity={}, duration={}",
					record.getContentId(), record.getUserIdentity(), record.getViewDuration());
			return false;
		}

		// 5. 检查24小时内是否已计数（去重）
		String userViewKey = USER_VIEW_PREFIX + record.getContentId() + ":" + record.getUserIdentity();
		Boolean isDuplicate = redisTemplate.hasKey(userViewKey);
		if (Boolean.TRUE.equals(isDuplicate)) {
			log.info("24小时内重复浏览，不计数: contentId={}, userIdentity={}",
					record.getContentId(), record.getUserIdentity());
			return false;
		}

		// 6. 记录浏览日志
		saveViewLog(record);

		// 7. 更新Redis计数器
		String countKey = VIEW_COUNT_PREFIX + record.getContentId();
		Long newCount = redisTemplate.opsForValue().increment(countKey);

		// 8. 设置用户浏览记录过期时间（24小时）
		redisTemplate.opsForValue().set(userViewKey, "1", duplicateTimeWindow, TimeUnit.SECONDS);

		// 9. 检查是否为热点内容，若是则实时同步到数据库
		checkAndSyncHotContent(record.getContentId(), countKey, newCount);

		log.info("成功记录有效浏览: contentId={}, userIdentity={}, newCount={}",
				record.getContentId(), record.getUserIdentity(), newCount);

		return true;
	}

	// 检查是否为有效浏览
	private boolean isValidView(ViewRecordDTO record) {
		log.info("检查是否为有效浏览: contentId={}, userIdentity={}, fromList={}, duration={}",
				record.getContentId(), record.getUserIdentity(), record.getFromList(), record.getViewDuration());
		if (record.getFromList()) {
			// 列表页浏览：检查时长和是否有交互
			return record.getViewDuration() >= listValidDuration
					&& Boolean.TRUE.equals(record.getHasInteraction());
		} else {
			// 详情页浏览：检查时长
			log.info("详情页浏览，检查时长: contentId={}, userIdentity={}, duration={}, validDuration:{}",
					record.getContentId(), record.getUserIdentity(), record.getViewDuration(), validDuration);
			return record.getViewDuration() >= validDuration;
		}
	}

	// 检查IP限流
	private boolean checkIpLimit(String ipAddress) {
		if (StringUtils.isEmpty(ipAddress)) {
			return false;
		}

		// 取IP的前三个段作为限流key（如192.168.1.xxx -> 192.168.1）
		String[] ipSegments = ipAddress.split("\\.");
		String ipPrefix = ipSegments.length >= 3 ?
				ipSegments[0] + "." + ipSegments[1] + "." + ipSegments[2] : ipAddress;

		String limitKey = IP_LIMIT_PREFIX + ipPrefix;

		// 使用Redis的incr和expire实现滑动窗口限流
		Long count = redisTemplate.opsForValue().increment(limitKey);
		if (count != null && count == 1) {
			redisTemplate.expire(limitKey, 60, TimeUnit.SECONDS);
		}

		return count != null && count <= ipLimitCount;
	}

	// 保存浏览日志
	private void saveViewLog(ViewRecordDTO record) {
		UserViewLog log = new UserViewLog();
		log.setContentId(record.getContentId());
		log.setUserIdentity(record.getUserIdentity());
		log.setViewTime(LocalDateTime.now());
		log.setViewDuration(record.getViewDuration());
		log.setIsValid(1);
		log.setIsFromList(record.getFromList() ? 1 : 0);
		log.setIpAddress(record.getIpAddress());
		log.setUserAgent(record.getUserAgent());

		userViewLogMapper.insert(log);
	}

	// 检查并同步热点内容
	private void checkAndSyncHotContent(Long contentId, String countKey, Long newCount) {
		if (newCount == null) {
			return;
		}

		// 记录1小时内的浏览增量
		String hotKey = HOT_CONTENT_PREFIX + contentId;
		Long hourlyIncrement = redisTemplate.opsForValue().increment(hotKey);
		if (hourlyIncrement != null && hourlyIncrement == 1) {
			redisTemplate.expire(hotKey, 1, TimeUnit.HOURS);
		}

		// 如果达到热点阈值，实时同步到数据库
		if (hourlyIncrement != null && hourlyIncrement >= hotContentThreshold) {
			syncToDatabase(contentId, countKey, newCount);
		}
	}

	// 同步到数据库
	private void syncToDatabase(Long contentId, String countKey, Long newCount) {
		try {
			// 检查记录是否存在
			ContentView contentView = contentViewMapper.selectOne(
					new QueryWrapper<ContentView>().eq("content_id", contentId));

			if (contentView == null) {
				// 新增记录
				contentView = new ContentView();
				contentView.setContentId(contentId);
				contentView.setViewCount(newCount);
				contentView.setTodayCount(1);
				contentView.setYesterdayCount(0);
				contentViewMapper.insert(contentView);
			} else {
				// 更新记录
				contentView.setViewCount(newCount);
				// 判断是否是今天
				LocalDate today = LocalDate.now();
				LocalDate updateDate = contentView.getUpdatedTime().toLocalDate();

				if (today.equals(updateDate)) {
					contentView.setTodayCount(contentView.getTodayCount() + 1);
				} else if (today.minusDays(1).equals(updateDate)) {
					contentView.setYesterdayCount(contentView.getTodayCount());
					contentView.setTodayCount(1);
				} else {
					contentView.setYesterdayCount(0);
					contentView.setTodayCount(1);
				}

				contentViewMapper.updateById(contentView);
			}

			log.info("热点内容浏览量同步到数据库: contentId={}, count={}", contentId, newCount);
		} catch (Exception e) {
			log.error("同步浏览量到数据库失败: contentId={}", contentId, e);
		}
	}

	@Override
	public Long getViewCount(Long contentId) {
		if (contentId == null) {
			return 0L;
		}

		String countKey = VIEW_COUNT_PREFIX + contentId;

		// 先从Redis获取
		Object countObj = redisTemplate.opsForValue().get(countKey);
		if (countObj != null) {
			return Long.parseLong(countObj.toString());
		}

		// Redis没有则从数据库获取
		LambdaQueryWrapper<ContentView> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ContentView::getContentId, contentId);
		ContentView contentView = this.getOne(queryWrapper);

		Long count = contentView != null ? contentView.getViewCount() : 0L;

		// 同步到Redis
		redisTemplate.opsForValue().set(countKey, count);

		return count;
	}

	@Override
	public Map<Long, Long> batchGetViewCounts(List<Long> contentIds) {
		if (CollectionUtils.isEmpty(contentIds)) {
			return Collections.emptyMap();
		}

		Map<Long, Long> result = new HashMap<>(contentIds.size());

		// 1. 批量从Redis获取
		List<String> keys = contentIds.stream()
				.map(id -> VIEW_COUNT_PREFIX + id)
				.collect(Collectors.toList());

		List<Object> counts = redisTemplate.opsForValue().multiGet(keys);

		// 2. 处理Redis结果
		List<Long> missContentIds = new ArrayList<>();

		for (int i = 0; i < contentIds.size(); i++) {
			Long contentId = contentIds.get(i);
			Object countObj = counts.get(i);

			if (countObj != null) {
				result.put(contentId, Long.parseLong(countObj.toString()));
			} else {
				missContentIds.add(contentId);
			}
		}

		// 3. 从数据库获取缺失的数据
		if (!missContentIds.isEmpty()) {
			List<ContentView> contentViews = contentViewMapper.selectList(
					new QueryWrapper<ContentView>().in("content_id", missContentIds));

			for (ContentView view : contentViews) {
				result.put(view.getContentId(), view.getViewCount());
				// 同步到Redis
				redisTemplate.opsForValue().set(VIEW_COUNT_PREFIX + view.getContentId(), view.getViewCount());
			}

			// 对于数据库中也没有的，设置为0
			for (Long id : missContentIds) {
				result.putIfAbsent(id, 0L);
			}
		}

		return result;
	}

	@Override
	@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
	public void cleanAndCorrectData() {
		log.info("开始执行数据清洗与校正任务");

		try {
			// 1. 批量同步Redis数据到数据库
			syncAllRedisDataToDb();

			// 2. 识别并清理无效数据
			cleanInvalidViews();

			log.info("数据清洗与校正任务执行完成");
		} catch (Exception e) {
			log.error("数据清洗与校正任务执行失败", e);
		}
	}

	@Override
	public Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params) {
		if (params == null || params.isEmpty()) {
			log.error("批量查询每日浏览量参数为空");
			return Collections.emptyMap();
		}

		// 1. 解析参数：获取文章ID列表和日期列表
		List<Long> contentIds = ConvertUtil.safeConvertToListOfLong(params.get("quoteIds"));
		List<LocalDate> dates = ConvertUtil.safeConvertToListOfLocalDate(params.get("dates"));

		// 参数校验：文章ID列表和日期列表不可为空
		if (CollectionUtils.isEmpty(contentIds) || CollectionUtils.isEmpty(dates)) {
			log.error("批量查询每日浏览量参数不完整：contentIds={}, dates={}", contentIds, dates);
			return Collections.emptyMap();
		}

		// 2. 调用Mapper查询指定日期和文章的有效浏览量总和（按日期分组）
		List<Map<String, Object>> dailyCounts = userViewLogMapper.selectDailyViewCounts(contentIds, dates);

		// 3. 转换查询结果为Map<LocalDate, Long>（日期→当日总浏览量）
		Map<LocalDate, Long> resultMap = new HashMap<>(dates.size());

		// 先初始化所有日期的计数为0
		for (LocalDate date : dates) {
			resultMap.put(date, 0L);
		}

		// 填充查询到的计数（覆盖初始的0）
		for (Map<String, Object> countMap : dailyCounts) {
			// 从查询结果中提取日期和计数

			LocalDate statDate = ConvertUtil.safeParseLocalDate(countMap.get("stat_date"));
			Long totalCount = ConvertUtil.safeParseLong(countMap.get("total_count"));

			if (statDate != null && resultMap.containsKey(statDate)) {
				resultMap.put(statDate, totalCount);
			}
		}

		log.info("批量查询每日浏览量完成：日期范围={}至{}, 文章数量={}, 结果={}",
				dates.get(0), dates.get(dates.size() - 1), contentIds.size(), resultMap);
		return resultMap;
	}

	// 批量同步Redis数据到数据库
	private void syncAllRedisDataToDb() {
		// 实现批量同步逻辑
		// 省略具体实现...
	}

	// 清理无效数据
	private void cleanInvalidViews() {
		// 1. 查询需要清理的无效浏览记录
		List<UserViewLog> invalidLogs = userViewLogMapper.selectList(
				new QueryWrapper<UserViewLog>()
						.eq("is_valid", false)
						.lt("create_time", LocalDateTime.now().minusDays(1)));

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

		// 3. 逐个校正内容的浏览量
		for (Map.Entry<Long, Long> entry : correctionMap.entrySet()) {
			Long contentId = entry.getKey();
			Long reduceCount = entry.getValue();

			// 更新数据库
			ContentView contentView = contentViewMapper.selectOne(
					new QueryWrapper<ContentView>().eq("content_id", contentId));

			if (contentView == null) {
				continue;
			}

			Long beforeCount = contentView.getViewCount();
			Long afterCount = Math.max(0, beforeCount - reduceCount);

			// 计算变化百分比
			int changePercent = (int) ((beforeCount - afterCount) * 100 / beforeCount);

			// 更新浏览量
			contentView.setViewCount(afterCount);
			contentViewMapper.updateById(contentView);

			// 更新Redis
			redisTemplate.opsForValue().set(VIEW_COUNT_PREFIX + contentId, afterCount);

			// 记录校正日志
			ViewCorrectionLog viewCorrectionLog = new ViewCorrectionLog();
			viewCorrectionLog.setContentId(contentId);
			viewCorrectionLog.setBeforeCount(beforeCount);
			viewCorrectionLog.setAfterCount(afterCount);
			viewCorrectionLog.setCorrectionTime(LocalDateTime.now());
			viewCorrectionLog.setReason("清理无效浏览记录");
			viewCorrectionLog.setOperator("system");
			correctionLogMapper.insert(viewCorrectionLog);

			log.info("校正浏览量: contentId={}, before={}, after={}, reduce={}",
					contentId, beforeCount, afterCount, reduceCount);

			// 如果变化超过阈值，发送通知
			if (changePercent >= correctionNotifyThreshold) {
				// sendCorrectionNotification(contentId, beforeCount, afterCount, changePercent);
			}
		}

		// 4. 删除清理掉的日志记录
		List<Long> logIds = invalidLogs.stream()
				.map(UserViewLog::getId)
				.collect(Collectors.toList());

		userViewLogMapper.deleteBatchIds(logIds);
		log.info("清理无效浏览记录: {} 条", logIds.size());
	}

	// 发送校正通知
	private void sendCorrectionNotification(Long contentId, Long beforeCount, Long afterCount, int changePercent) {
		Long authorId = getAuthorIdByContentId(contentId);

		if (authorId == null) {
			log.warn("无法获取内容作者信息: contentId={}", contentId);
			return;
		}

		// 2. 构建通知消息
		// ViewCorrectionNotificationDTO notification = new ViewCorrectionNotificationDTO();
		// notification.setUserId(authorId);
		// notification.setContentId(contentId);
		// notification.setBeforeCount(beforeCount);
		// notification.setAfterCount(afterCount);
		// notification.setChangePercent(changePercent);
		// notification.setNotifyTime(LocalDateTime.now());
		// notification.setTitle("内容浏览量校正通知");
		// notification.setContent(String.format(
		// 		"您的内容（ID: %d）浏览量因系统清理无效数据已校正，从 %d 调整为 %d，变动率为 %d%%。",
		// 		contentId, beforeCount, afterCount, changePercent));
		//
		// // 3. 发送到消息队列
		// rabbitTemplate.convertAndSend(
		// 		"view.correction.notify.exchange",
		// 		"view.correction.notify.key",
		// 		notification);

		log.info("发送浏览量校正通知: userId={}, contentId={}", authorId, contentId);
	}

	// 获取内容作者ID（实际应通过Feign调用content-service）
	private Long getAuthorIdByContentId(Long contentId) {
		return null;
	}
}