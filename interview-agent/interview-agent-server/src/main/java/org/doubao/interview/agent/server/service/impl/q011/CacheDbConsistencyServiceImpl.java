package org.doubao.interview.agent.server.service.impl.q011;

import org.doubao.interview.agent.api.dto.q011.CacheDbConsistencyQueryRequest;
import org.doubao.interview.agent.api.dto.q011.CacheDbConsistencyResponse;
import org.doubao.interview.agent.api.dto.q011.CacheDbConsistencyUpdateRequest;
import org.doubao.interview.agent.api.service.q011.CacheDbConsistencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 问题011：缓存与数据库双写一致性示例实现。
 * <p>
 * 核心策略说明：
 * 1. 主流程采用“写库后删缓存”，避免“先删缓存再写库”的并发脏窗口放大问题。
 * 2. 删除缓存失败时，立即进入重试补偿队列，尽快清理旧缓存。
 * 3. 增加“延迟双删”作为第二次兜底，进一步缩小并发窗口影响。
 * 4. 同时模拟MQ失效通知，让其他节点也能感知并清理本地/远端缓存。
 * 5. 通过TTL兜底保障最终一致：即使补偿短时失败，旧缓存也会随时间淘汰。
 * <p>
 * 本类是教学版最小闭环实现：
 * - database: 模拟数据库
 * - redisCache: 模拟共享缓存
 * - localCache: 模拟应用本地缓存
 * - mqBus: 模拟异步失效事件总线
 */
@Service
public class CacheDbConsistencyServiceImpl implements CacheDbConsistencyService {

    private static final Logger log = LoggerFactory.getLogger(CacheDbConsistencyServiceImpl.class);

    /** 共享缓存TTL：用于最终一致兜底。 */
    private static final long REDIS_TTL_MILLIS = 15_000L;
    /** 本地缓存TTL：更短，降低脏数据停留时间。 */
    private static final long LOCAL_TTL_MILLIS = 3_000L;
    /** 删除缓存失败后的最大重试次数。 */
    private static final int DELETE_RETRY_MAX = 3;
    /** 延迟双删等待时间。 */
    private static final long DELAY_DELETE_MILLIS = 200L;

    private final Map<String, String> database = new ConcurrentHashMap<String, String>();
    private final Map<String, CacheEntry> redisCache = new ConcurrentHashMap<String, CacheEntry>();
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<String, CacheEntry>();
    private final Map<String, DeleteRetryTask> retryTasks = new ConcurrentHashMap<String, DeleteRetryTask>();
    private final Map<String, MqInvalidationEvent> mqBus = new ConcurrentHashMap<String, MqInvalidationEvent>();
    private final Map<String, AtomicBoolean> delayDeleteRunning = new ConcurrentHashMap<String, AtomicBoolean>();

    @PostConstruct
    public void init() {
        long now = Instant.now().toEpochMilli();
        putSeed("profile-1001", "用户资料V1", now);
        putSeed("profile-1002", "用户资料V1", now);
    }

    @Override
    public CacheDbConsistencyResponse update(CacheDbConsistencyUpdateRequest request) {
        long start = System.currentTimeMillis();
        String dataId = request == null ? null : request.getDataId();
        String newValue = request == null ? null : request.getNewValue();
        boolean simulateDeleteFail = request != null && request.isSimulateDeleteFail();

        if (!isValidDataId(dataId) || newValue == null || newValue.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "参数非法，更新终止", dataId, null, null, false, start);
        }

        // Step 1：先写数据库（事务提交视角）。
        String dbValue = newValue + "@" + Instant.now().toEpochMilli();
        database.put(dataId, dbValue);

        // Step 2：写库后删除缓存（推荐策略）。
        boolean deleted = invalidateCaches(dataId, simulateDeleteFail);
        if (!deleted) {
            // 删除失败时进入重试补偿，保障最终一致。
            enqueueRetry(dataId);
        }

        // Step 3：延迟双删作为第二道保险，进一步降低并发窗口概率。
        startDelayDelete(dataId);

        // Step 4：发布MQ失效事件，模拟跨节点异步失效通知。
        publishInvalidationEvent(dataId);

        String cacheSnapshot = snapshotCacheValue(dataId);
        boolean eventuallyConsistent = deleted || retryTasks.containsKey(dataId);
        String msg = deleted
                ? "写库后删缓存成功，并触发延迟双删与MQ失效通知"
                : "首次删缓存失败，已进入重试补偿并触发延迟双删与MQ失效通知";

        return build(true, deleted ? "UPDATE_DB_THEN_DELETE_CACHE" : "DELETE_CACHE_RETRY_ENQUEUED",
                msg, dataId, dbValue, cacheSnapshot, eventuallyConsistent, start);
    }

    @Override
    public CacheDbConsistencyResponse query(CacheDbConsistencyQueryRequest request) {
        long start = System.currentTimeMillis();
        String dataId = request == null ? null : request.getDataId();
        if (!isValidDataId(dataId)) {
            return build(false, "PARAM_VALIDATION", "参数非法，查询终止", dataId, null, null, false, start);
        }

        long now = Instant.now().toEpochMilli();

        // 第一层：本地缓存。
        CacheEntry local = localCache.get(dataId);
        if (local != null && local.expireAt > now) {
            return build(true, "LOCAL_CACHE_HIT", "命中本地缓存", dataId, database.get(dataId), local.value, true, start);
        }

        // 第二层：共享缓存。
        CacheEntry redis = redisCache.get(dataId);
        if (redis != null && redis.expireAt > now) {
            localCache.put(dataId, new CacheEntry(redis.value, now + LOCAL_TTL_MILLIS));
            return build(true, "REDIS_CACHE_HIT", "命中共享缓存并回填本地缓存", dataId, database.get(dataId), redis.value, true, start);
        }

        // 第三层：回源数据库并重建缓存（读路径最终一致）。
        String dbValue = database.get(dataId);
        if (dbValue == null) {
            return build(false, "DB_MISS", "数据库无记录", dataId, null, null, false, start);
        }

        redisCache.put(dataId, new CacheEntry(dbValue, now + REDIS_TTL_MILLIS));
        localCache.put(dataId, new CacheEntry(dbValue, now + LOCAL_TTL_MILLIS));
        return build(true, "DB_RELOAD", "缓存未命中，已回源数据库并重建缓存", dataId, dbValue, dbValue, true, start);
    }

    /**
     * 统一删除本地缓存和共享缓存。
     * <p>
     * 返回值语义：
     * - true：删除动作成功完成
     * - false：模拟删除失败，需要进入补偿流程
     */
    private boolean invalidateCaches(String dataId, boolean simulateDeleteFail) {
        if (simulateDeleteFail) {
            return false;
        }
        localCache.remove(dataId);
        redisCache.remove(dataId);
        return true;
    }

    /**
     * 删除失败重试队列。
     * <p>
     * 教学说明：
     * 生产环境通常由调度器/消息重试驱动，这里用线程模拟异步补偿。
     */
    private void enqueueRetry(String dataId) {
        DeleteRetryTask task = retryTasks.computeIfAbsent(dataId, k -> new DeleteRetryTask());
        new Thread(() -> {
            while (task.retryCount.incrementAndGet() <= DELETE_RETRY_MAX) {
                localCache.remove(dataId);
                redisCache.remove(dataId);
                task.success.set(true);
                break;
            }
            if (task.success.get()) {
                retryTasks.remove(dataId);
                log.info("q011 delete-cache retry success, dataId={}, retryCount={}", dataId, task.retryCount.get());
            } else {
                log.warn("q011 delete-cache retry exhausted, dataId={}, retryCount={}", dataId, task.retryCount.get());
            }
        }, "q011-delete-retry-" + dataId).start();
    }

    /**
     * 延迟双删：延迟一小段时间后再删一次缓存。
     * <p>
     * 目的：
     * 当并发读请求在“写库后、删缓存前”或“删缓存后立刻回填旧值”的窗口内发生时，
     * 第二次删除有机会清掉这份旧缓存。
     */
    private void startDelayDelete(String dataId) {
        AtomicBoolean running = delayDeleteRunning.computeIfAbsent(dataId, k -> new AtomicBoolean(false));
        if (!running.compareAndSet(false, true)) {
            return;
        }

        new Thread(() -> {
            try {
                Thread.sleep(DELAY_DELETE_MILLIS);
                localCache.remove(dataId);
                redisCache.remove(dataId);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                running.set(false);
            }
        }, "q011-delay-double-delete-" + dataId).start();
    }

    /**
     * 发布缓存失效事件（MQ模拟）。
     */
    private void publishInvalidationEvent(String dataId) {
        MqInvalidationEvent event = new MqInvalidationEvent(dataId, Instant.now().toEpochMilli());
        mqBus.put(dataId, event);
        // 模拟消费者异步处理：收到事件后删除缓存。
        new Thread(() -> {
            localCache.remove(dataId);
            redisCache.remove(dataId);
        }, "q011-mq-invalidation-consumer-" + dataId).start();
    }

    private void putSeed(String dataId, String value, long now) {
        database.put(dataId, value + "@" + now);
        redisCache.put(dataId, new CacheEntry(value + "@" + now, now + REDIS_TTL_MILLIS));
        localCache.put(dataId, new CacheEntry(value + "@" + now, now + LOCAL_TTL_MILLIS));
    }

    private String snapshotCacheValue(String dataId) {
        long now = Instant.now().toEpochMilli();
        CacheEntry local = localCache.get(dataId);
        if (local != null && local.expireAt > now) {
            return local.value;
        }
        CacheEntry redis = redisCache.get(dataId);
        if (redis != null && redis.expireAt > now) {
            return redis.value;
        }
        return null;
    }

    private boolean isValidDataId(String dataId) {
        return dataId != null && dataId.matches("^[a-zA-Z0-9_-]{3,64}$");
    }

    private CacheDbConsistencyResponse build(boolean success, String stage, String message,
                                             String dataId, String dbValue, String cacheValue,
                                             boolean eventuallyConsistent, long start) {
        long cost = System.currentTimeMillis() - start;
        log.info("q011-consistency stage={}, dataId={}, dbValue={}, cacheValue={}, eventuallyConsistent={}, costMs={}, message={}",
                stage, dataId, dbValue, cacheValue, eventuallyConsistent, cost, message);
        CacheDbConsistencyResponse response = new CacheDbConsistencyResponse();
        response.setSuccess(success);
        response.setStage(stage);
        response.setMessage(message);
        response.setDataId(dataId);
        response.setDbValue(dbValue);
        response.setCacheValue(cacheValue);
        response.setEventuallyConsistent(eventuallyConsistent);
        return response;
    }

    /**
     * 缓存实体。
     */
    private static class CacheEntry {
        private final String value;
        private final long expireAt;

        private CacheEntry(String value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }

    /**
     * 删除失败重试任务实体。
     */
    private static class DeleteRetryTask {
        private final AtomicInteger retryCount = new AtomicInteger(0);
        private final AtomicBoolean success = new AtomicBoolean(false);
    }

    /**
     * 缓存失效MQ事件实体。
     */
    private static class MqInvalidationEvent {
        private final String dataId;
        private final long eventTime;

        private MqInvalidationEvent(String dataId, long eventTime) {
            this.dataId = dataId;
            this.eventTime = eventTime;
        }
    }
}
