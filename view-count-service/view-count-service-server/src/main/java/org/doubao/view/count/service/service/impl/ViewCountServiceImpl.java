package org.doubao.view.count.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.util.ConvertUtil;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.view.count.service.dto.ViewRecordDTO;
import org.doubao.view.count.service.entity.ContentView;
import org.doubao.view.count.service.entity.UserViewLog;
import org.doubao.view.count.service.mapper.ContentViewMapper;
import org.doubao.view.count.service.service.UserViewLogService;
import org.doubao.view.count.service.service.ViewCorrectionLogService;
import org.doubao.view.count.service.service.ViewCountService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class ViewCountServiceImpl extends ServiceImpl<ContentViewMapper, ContentView> implements ViewCountService {

	private static final Logger log = LoggerFactory.getLogger(ViewCountServiceImpl.class);
	// 分布式锁前缀
	private static final String LOCK_PREFIX = "view:lock:";
	// 缓存空值过期时间（防穿透）
	private static final long EMPTY_CACHE_EXPIRE = 5 * 60;
	// 缓存过期时间随机偏移量（防雪崩）
	private static final int RANDOM_EXPIRE_OFFSET = 600;

	@Autowired
	private ContentViewMapper contentViewMapper;

	@Autowired
	private UserViewLogService userViewLogService;

	@Autowired
	private ViewCorrectionLogService viewCorrectionLogService;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RedissonClient redissonClient;

	// 有效浏览时长（秒）
	@Value("${view-count.valid-duration:5}")
	private int validDuration;

	// 列表页有效浏览时长（秒）
	@Value("${view-count.list-valid-duration:3}")
	private int listValidDuration;

	// 去重时间窗口（秒）
	@Value("${view-count.duplicate-time-window:86400}")
	private int duplicateTimeWindow;

	// 热点内容阈值（1小时增量）
	@Value("${view-count.hot-content-threshold:1000}")
	private int hotContentThreshold;

	// IP限流次数
	@Value("${view-count.ip-limit-count:10}")
	private int ipLimitCount;

	// IP限流周期（秒）
	@Value("${view-count.ip-limit-period:60}")
	private int ipLimitPeriod;

	// 校正通知阈值（百分比）
	@Value("${view-count.correction-notify-threshold:10}")
	private int correctionNotifyThreshold;

	// Redis key 前缀
	private static final String VIEW_COUNT_PREFIX = "view:count:";
	private static final String USER_VIEW_PREFIX = "view:user:";
	private static final String IP_LIMIT_PREFIX = "view:ip:limit:";
	private static final String HOT_CONTENT_PREFIX = "view:hot:";
	private static final String EMPTY_CONTENT_PREFIX = "view:empty:";

	// 本地锁（防止单机重复执行）
	private final Lock localLock = new ReentrantLock();

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean recordView(ViewRecordDTO record) {
		// 1. 验证参数
		if (DoubaoUtils.isEmpty(record.getUserIdentity())) {
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
			// 记录无效日志（is_valid=0）
			saveInvalidViewLog(record);
			return false;
		}

		// 5. 检查重复浏览（去重）
		String userViewKey = USER_VIEW_PREFIX + record.getContentId() + ":" + record.getUserIdentity();
		Boolean isDuplicate = redisTemplate.hasKey(userViewKey);
		if (Boolean.TRUE.equals(isDuplicate)) {
			log.info("{}秒内重复浏览，不计数: contentId={}, userIdentity={}",
					duplicateTimeWindow, record.getContentId(), record.getUserIdentity());
			return false;
		}

		// 6. 记录有效浏览日志
		saveViewLog(record);

		// 7. 更新Redis计数器（添加随机过期时间防雪崩）
		String countKey = VIEW_COUNT_PREFIX + record.getContentId();
		Long newCount = redisTemplate.opsForValue().increment(countKey);
		// 设置过期时间（24小时 + 随机偏移）
		long expireTime = 24 * 3600 + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
		redisTemplate.expire(countKey, expireTime, TimeUnit.SECONDS);

		// 8. 设置用户浏览记录过期时间
		redisTemplate.opsForValue().set(userViewKey, "1", duplicateTimeWindow, TimeUnit.SECONDS);

		// 9. 检查并同步热点内容（加分布式锁防止并发同步）
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

	// 检查IP限流（修复配置参数使用错误）
	private boolean checkIpLimit(String ipAddress) {
		if (DoubaoUtils.isEmpty(ipAddress)) {
			return false;
		}

		// 取IP的前三个段作为限流key（如192.168.1.xxx -> 192.168.1）
		String[] ipSegments = ipAddress.split("\\.");
		String ipPrefix = ipSegments.length >= 3 ?
				ipSegments[0] + "." + ipSegments[1] + "." + ipSegments[2] : ipAddress;

		String limitKey = IP_LIMIT_PREFIX + ipPrefix;

		// 使用Redis的incr和expire实现滑动窗口限流（使用配置的ipLimitPeriod）
		Long count = redisTemplate.opsForValue().increment(limitKey);
		if (count != null && count == 1) {
			redisTemplate.expire(limitKey, ipLimitPeriod, TimeUnit.SECONDS);
		}

		return count != null && count <= ipLimitCount;
	}

	// 保存有效浏览日志
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

		userViewLogService.save(log);
	}

	// 保存无效浏览日志
	private void saveInvalidViewLog(ViewRecordDTO record) {
		UserViewLog log = new UserViewLog();
		log.setContentId(record.getContentId());
		log.setUserIdentity(record.getUserIdentity());
		log.setViewTime(LocalDateTime.now());
		log.setViewDuration(record.getViewDuration());
		log.setIsValid(0);
		log.setIsFromList(record.getFromList() ? 1 : 0);
		log.setIpAddress(record.getIpAddress());
		log.setUserAgent(record.getUserAgent());

		userViewLogService.save(log);
	}

	// 检查并同步热点内容（添加分布式锁）
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
			// 分布式锁：防止同一内容并发同步
			String lockKey = LOCK_PREFIX + "hot:" + contentId;
			RLock lock = redissonClient.getLock(lockKey);
			try {
				// 尝试获取锁（5秒等待，10秒自动释放）
				if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
					syncToDatabase(contentId, countKey, newCount);
				}
			} catch (InterruptedException e) {
				log.error("获取热点内容同步锁失败: contentId={}", contentId, e);
				Thread.currentThread().interrupt();
			} finally {
				if (lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		}
	}

	// 同步到数据库
	private void syncToDatabase(Long contentId, String countKey, Long newCount) {
		try {
			LambdaQueryWrapper<ContentView> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(ContentView::getContentId, contentId);
			ContentView contentView = contentViewMapper.selectOne(queryWrapper);

			if (contentView == null) {
				// 新增记录
				contentView = new ContentView();
				contentView.setContentId(contentId);
				contentView.setViewCount(newCount);
				contentView.setTodayCount(1);
				contentView.setYesterdayCount(0);
				contentView.setCreatedTime(LocalDateTime.now());
				contentView.setUpdatedTime(LocalDateTime.now());
				contentViewMapper.insert(contentView);

				// 同步Redis的浏览量，添加随机过期时间
				long expireTime = 24 * 3600 + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
				redisTemplate.opsForValue().set(countKey, newCount, expireTime, TimeUnit.SECONDS);
			} else {
				LocalDate today = LocalDate.now();
				LocalDate updateDate = contentView.getUpdatedTime() != null ?
						contentView.getUpdatedTime().toLocalDate() : LocalDate.now().minusDays(1);

				int todayCount = contentView.getTodayCount();
				int yesterdayCount = contentView.getYesterdayCount();

				if (today.equals(updateDate)) {
					todayCount += 1;
				} else if (today.minusDays(1).equals(updateDate)) {
					yesterdayCount = contentView.getTodayCount();
					todayCount = 1;
				} else {
					yesterdayCount = 0;
					todayCount = 1;
				}

				// 直接更新
				LambdaUpdateWrapper<ContentView> updateWrapper = new LambdaUpdateWrapper<>();
				updateWrapper.eq(ContentView::getContentId, contentId)
						.set(ContentView::getViewCount, newCount)
						.set(ContentView::getTodayCount, todayCount)
						.set(ContentView::getYesterdayCount, yesterdayCount)
						.set(ContentView::getUpdatedTime, LocalDateTime.now());

				int updateRows = contentViewMapper.update(null, updateWrapper);
				if (updateRows == 0) {
					log.warn("浏览量更新失败: contentId={}", contentId);
				}

				// 同步Redis的浏览量，添加随机过期时间
				long expireTime = 24 * 3600 + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
				redisTemplate.opsForValue().set(countKey, newCount, expireTime, TimeUnit.SECONDS);
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
		String emptyKey = EMPTY_CONTENT_PREFIX + contentId;

		// 1. 检查是否是缓存空值（防穿透）
		if (Boolean.TRUE.equals(redisTemplate.hasKey(emptyKey))) {
			return 0L;
		}

		// 2. 先从Redis获取
		Object countObj = redisTemplate.opsForValue().get(countKey);
		if (DoubaoUtils.isNotEmpty(countObj)) {
			try {
				return Long.parseLong(countObj.toString());
			} catch (NumberFormatException e) {
				log.warn("Redis中浏览量数据格式错误: key={}, value={}", countKey, countObj);
			}
		}

		// 3. Redis没有则从数据库获取（加本地锁防止缓存击穿）
		localLock.lock();
		try {
			// 双重检查：防止多线程重复查询数据库
			countObj = redisTemplate.opsForValue().get(countKey);
			if (countObj != null) {
				return Long.parseLong(countObj.toString());
			}

			LambdaQueryWrapper<ContentView> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(ContentView::getContentId, contentId);
			ContentView contentView = this.getOne(queryWrapper);

			Long count = contentView != null ? contentView.getViewCount() : 0L;

			// 4. 同步到Redis（防穿透：空值设置短过期，非空设置随机过期）
			if (count == 0) {
				redisTemplate.opsForValue().set(emptyKey, "1", EMPTY_CACHE_EXPIRE, TimeUnit.SECONDS);
			} else {
				long expireTime = 24 * 3600 + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
				redisTemplate.opsForValue().set(countKey, count, expireTime, TimeUnit.SECONDS);
			}

			return count;
		} finally {
			localLock.unlock();
		}
	}

	@Override
	public Map<Long, Long> batchGetViewCounts(List<Long> contentIds) {
		if (CollectionUtils.isEmpty(contentIds)) {
			return Collections.emptyMap();
		}

		Map<Long, Long> result = new HashMap<>(contentIds.size());
		List<Long> needFetchFromDb = new ArrayList<>();

		// 1. Redis批量获取（mget提升效率）
		List<String> countKeys = contentIds.stream()
				.map(id -> VIEW_COUNT_PREFIX + id)
				.collect(Collectors.toList());
		List<Object> countObjs = redisTemplate.opsForValue().multiGet(countKeys);

		// 2. 解析Redis结果
		for (int i = 0; i < contentIds.size(); i++) {
			Long contentId = contentIds.get(i);
			Object countObj = DoubaoUtils.notNull(countObjs) ? countObjs.get(i) : null;

			// 检查空值缓存
			if (Boolean.TRUE.equals(redisTemplate.hasKey(EMPTY_CONTENT_PREFIX + contentId))) {
				result.put(contentId, 0L);
				continue;
			}

			if (DoubaoUtils.isNotEmpty(countObj)) {
				try {
					result.put(contentId, Long.parseLong(countObj.toString()));
				} catch (NumberFormatException e) {
					log.warn("Redis中浏览量数据格式错误: contentId={}, value={}", contentId, countObj);
					needFetchFromDb.add(contentId);
				}
			} else {
				needFetchFromDb.add(contentId);
			}
		}

		// 3. 批量从数据库获取缺失数据
		if (!CollectionUtils.isEmpty(needFetchFromDb)) {
			LambdaQueryWrapper<ContentView> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.in(ContentView::getContentId, needFetchFromDb);
			List<ContentView> contentViews = contentViewMapper.selectList(queryWrapper);

			// 构建数据库结果映射
			Map<Long, Long> dbResult = contentViews.stream()
					.collect(Collectors.toMap(ContentView::getContentId, ContentView::getViewCount));

			// 4. 同步到Redis并更新结果
			Map<String, Object> redisBatchSet = new HashMap<>();
			for (Long contentId : needFetchFromDb) {
				Long count = dbResult.getOrDefault(contentId, 0L);
				result.put(contentId, count);

				// 防穿透：空值设置短过期，非空设置随机过期
				if (count == 0) {
					redisTemplate.opsForValue().set(EMPTY_CONTENT_PREFIX + contentId, "1", EMPTY_CACHE_EXPIRE, TimeUnit.SECONDS);
				} else {
					String countKey = VIEW_COUNT_PREFIX + contentId;
					long expireTime = 24 * 3600 + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
					redisBatchSet.put(countKey, count);
					redisTemplate.expire(countKey, expireTime, TimeUnit.SECONDS);
				}
			}

			// 批量设置Redis（减少交互）
			if (!redisBatchSet.isEmpty()) {
				redisTemplate.opsForValue().multiSet(redisBatchSet);
			}
		}

		return result;
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
		List<Map<String, Object>> dailyCounts = userViewLogService.selectDailyViewCounts(contentIds, dates);

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
}