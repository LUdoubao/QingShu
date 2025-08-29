package org.doubao.fanout.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FansQueryService {

	private static final Logger log = LoggerFactory.getLogger(FansQueryService.class);
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	// 配置参数
	private final long vipThreshold;
	private final int batchSize;
	private final String fansKeyPrefix;
	private final String countKeyPrefix;
	private final long cacheTtl;

	public FansQueryService(
							@Value("${fanout.fans.vip-threshold:50000}") long vipThreshold,
							@Value("${fanout.fans.batch-size:1000}") int batchSize,
							@Value("${fanout.cache.redis.fans-key-prefix:follow:fans:}") String fansKeyPrefix,
							@Value("${fanout.cache.redis.count-key-prefix:follow:count:}") String countKeyPrefix,
							@Value("${fanout.cache.redis.ttl:3600}") long cacheTtl) {
		this.vipThreshold = vipThreshold;
		this.batchSize = batchSize;
		this.fansKeyPrefix = fansKeyPrefix;
		this.countKeyPrefix = countKeyPrefix;
		this.cacheTtl = cacheTtl;
	}

	/**
	 * 查询有效的粉丝列表
	 */
	public List<Long> queryValidFans(Long followeeId) {
		if (followeeId == null) {
			log.warn("[queryValidFans] followeeId is null");
			return Collections.emptyList();
		}

		try {
			// 查询粉丝总量
			Long totalFans = queryTotalFans(followeeId);
			if (totalFans <= 0) {
				log.info("[queryValidFans] No fans found for followeeId: {}", followeeId);
				return Collections.emptyList();
			}

			// 大V不参与分发
			if (totalFans > vipThreshold) {
				log.info("[queryValidFans] VIP user with too many fans, followeeId: {}, totalFans: {}",
						followeeId, totalFans);
				return Collections.emptyList();
			}

			// 查询粉丝ID列表
			String cacheKey = fansKeyPrefix + followeeId;
			Set<Object> fansIdSet = redisTemplate.opsForSet().members(cacheKey);

			if (!CollectionUtils.isEmpty(fansIdSet)) {
				return fansIdSet.stream()
						.map(id -> Long.parseLong(id.toString()))
						.collect(Collectors.toList());
			}

			// 缓存未命中，从DB查询并更新缓存
			List<Long> fansList = queryFansFromDb(followeeId, totalFans);
			if (!CollectionUtils.isEmpty(fansList)) {
				redisTemplate.opsForSet().add(cacheKey, fansList.toArray());
				redisTemplate.expire(cacheKey, cacheTtl, TimeUnit.SECONDS);
				log.info("[queryValidFans] Cached fans for followeeId: {}, count: {}", followeeId, fansList.size());
			}

			return fansList;
		} catch (Exception e) {
			log.error("[queryValidFans] Error querying fans for followeeId: {}", followeeId, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 查询粉丝总量
	 */
	private Long queryTotalFans(Long followeeId) {
		try {
			String countKey = countKeyPrefix + followeeId;
			Object countObj = redisTemplate.opsForValue().get(countKey);

			if (countObj != null) {
				return Long.parseLong(countObj.toString());
			}

			// 缓存未命中，从DB查询 TODO
			// Long count = followsMapper.countFollowers(followeeId);
			Long count = 0L;
			if (count == null) {
				count = 0L;
			}

			redisTemplate.opsForValue().set(countKey, count.toString(), 1, TimeUnit.HOURS);
			return count;
		} catch (Exception e) {
			log.error("[queryTotalFans] Error querying fan count for followeeId: {}", followeeId, e);
			return 0L;
		}
	}

	/**
	 * 从数据库查询粉丝
	 */
	private List<Long> queryFansFromDb(Long followeeId, Long totalFans) {
		List<Long> allFans = new ArrayList<>();
		int offset = 0;

		try {
			do {
				// TODO
				// List<Long> batchFans = followsMapper.selectFollowerIds(followeeId, offset, batchSize);
				List<Long> batchFans = new ArrayList<>() ;
				if (CollectionUtils.isEmpty(batchFans)) {
					break;
				}

				allFans.addAll(batchFans);
				offset += batchSize;

				// 防止无限循环，增加安全检查
				if (offset > totalFans + batchSize) {
					log.warn("[queryFansFromDb] Exceeded expected total fans, followeeId: {}, expected: {}, actual: {}",
							followeeId, totalFans, allFans.size());
					break;
				}
			} while (offset < totalFans);

			log.info("[queryFansFromDb] Queried {} fans from DB for followeeId: {}", allFans.size(), followeeId);
			return allFans;
		} catch (Exception e) {
			log.error("[queryFansFromDb] Error querying fans from DB for followeeId: {}", followeeId, e);
			return allFans;
		}
	}
}

