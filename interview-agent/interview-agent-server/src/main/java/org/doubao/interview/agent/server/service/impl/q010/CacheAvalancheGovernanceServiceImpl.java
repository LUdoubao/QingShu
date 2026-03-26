package org.doubao.interview.agent.server.service.impl.q010;

import org.doubao.interview.agent.api.dto.q010.CacheAvalancheQueryRequest;
import org.doubao.interview.agent.api.dto.q010.CacheAvalancheQueryResponse;
import org.doubao.interview.agent.api.service.q010.CacheAvalancheGovernanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 问题010：缓存雪崩治理示例实现。
 * <p>
 * 本实现围绕“缓存雪崩”五个核心策略构建完整链路：
 * 1. TTL 加随机值：写入缓存时增加随机抖动，避免大量 key 同时过期。
 * 2. 多级缓存：先查本地缓存，再查 Redis（此处用内存模拟）。
 * 3. 预热与异步续期：启动时加载关键数据，请求命中时在临期阈值内异步续期。
 * 4. 限流 + 降级 + 熔断：当缓存层或下游异常时保护数据库。
 * 5. Redis 高可用：模拟多节点集群，不依赖单点缓存节点。
 * <p>
 * 说明：当前是最小可运行教学实现，便于面试题代码落地与链路验证。
 */
@Service
public class CacheAvalancheGovernanceServiceImpl implements CacheAvalancheGovernanceService {

    private static final Logger log = LoggerFactory.getLogger(CacheAvalancheGovernanceServiceImpl.class);

    /** 本地缓存基础TTL。 */
    private static final long LOCAL_CACHE_BASE_TTL_MILLIS = 2_000L;
    /** Redis缓存基础TTL。 */
    private static final long REDIS_CACHE_BASE_TTL_MILLIS = 15_000L;
    /** TTL随机抖动上限。 */
    private static final int TTL_RANDOM_BOUND_MILLIS = 5_000;
    /** 临期阈值：剩余TTL低于该值时触发异步续期。 */
    private static final long RENEW_THRESHOLD_MILLIS = 2_500L;
    /** 限流阈值：每个client每秒最多10次。 */
    private static final int RATE_LIMIT_PER_SECOND = 10;
    /** 熔断触发阈值：连续失败达到阈值则打开熔断。 */
    private static final int CIRCUIT_OPEN_THRESHOLD = 4;
    /** 熔断打开时长。 */
    private static final long CIRCUIT_OPEN_DURATION_MILLIS = 10_000L;

    private final Random random = new Random();

    /** 模拟数据库。 */
    private final Map<String, String> database = new ConcurrentHashMap<>();
    /** 多级缓存中的本地层。 */
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();
    /** 多级缓存中的Redis层（内存模拟）。 */
    private final Map<String, CacheEntry> redisCache = new ConcurrentHashMap<>();
    /** 降级兜底数据。 */
    private final Map<String, String> degradeFallback = new ConcurrentHashMap<>();
    /** 每个 key 的异步续期并发控制。 */
    private final Map<String, AtomicBoolean> renewFlags = new ConcurrentHashMap<>();
    /** 每个 key 的互斥重建锁。 */
    private final Map<String, Object> rebuildLocks = new ConcurrentHashMap<>();
    /** 客户端限流计数窗口。 */
    private final Map<String, CounterWindow> counters = new ConcurrentHashMap<>();

    /** 熔断器状态。 */
    private final CircuitBreaker circuitBreaker = new CircuitBreaker();
    /** Redis集群模拟对象，体现高可用概念。 */
    private final RedisClusterMock redisCluster = new RedisClusterMock();

    @PostConstruct
    public void init() {
        // 关键数据预热：启动即写入DB、降级池和缓存层，减少冷启动回源压力。
        List<String> hotKeys = Arrays.asList("goods-1001", "goods-1002", "goods-1003");
        long now = Instant.now().toEpochMilli();
        for (String key : hotKeys) {
            String value = "商品缓存数据:" + key + "@" + now;
            database.put(key, value);
            degradeFallback.put(key, "降级静态数据:" + key);
            writeRedisWithRandomTtl(key, value, now);
            writeLocalCache(key, value, now);
        }
    }

    @Override
    public CacheAvalancheQueryResponse query(CacheAvalancheQueryRequest request) {
        long start = System.currentTimeMillis();
        String bizKey = request == null ? null : request.getBizKey();
        String clientId = request == null ? null : request.getClientId();
        boolean simulateRedisDown = request != null && request.isSimulateRedisDown();

        if (!isValidBizKey(bizKey)) {
            return build(false, "PARAM_VALIDATION", "业务key非法，已前置拦截", null, false,
                    circuitBreaker.isOpen(), start, bizKey, clientId);
        }

        if (!allow(clientId == null ? "unknown" : clientId)) {
            return build(false, "RATE_LIMIT", "触发限流，阻断突发流量对下游冲击", null, true,
                    circuitBreaker.isOpen(), start, bizKey, clientId);
        }

        if (circuitBreaker.isOpen()) {
            return degradeResponse("CIRCUIT_OPEN", "熔断已打开，直接返回降级数据", start, bizKey, clientId);
        }

        long now = Instant.now().toEpochMilli();

        // 第一层：本地缓存，优先吸收热点流量。
        CacheEntry local = localCache.get(bizKey);
        if (local != null && local.expireAt > now) {
            tryAsyncRenew(bizKey, local, now, "LOCAL_CACHE");
            return build(true, "LOCAL_CACHE_HIT", "命中本地缓存，降低Redis与数据库压力", local.data,
                    false, circuitBreaker.isOpen(), start, bizKey, clientId);
        }

        // 模拟Redis集群可用性（请求级开关 + 集群节点状态）。
        boolean redisAvailable = redisCluster.isAvailable(!simulateRedisDown);
        if (redisAvailable) {
            CacheEntry redisEntry = redisCache.get(bizKey);
            if (redisEntry != null && redisEntry.expireAt > now) {
                writeLocalCache(bizKey, redisEntry.data, now);
                tryAsyncRenew(bizKey, redisEntry, now, "REDIS_CACHE");
                circuitBreaker.recordSuccess();
                return build(true, "REDIS_CACHE_HIT", "命中Redis缓存并回填本地缓存", redisEntry.data,
                        false, circuitBreaker.isOpen(), start, bizKey, clientId);
            }
        }

        // Redis不可用或未命中时，进入互斥重建，避免并发回源雪崩。
        Object lock = rebuildLocks.computeIfAbsent(bizKey, k -> new Object());
        synchronized (lock) {
            now = Instant.now().toEpochMilli();
            CacheEntry secondRead = redisCache.get(bizKey);
            if (redisAvailable && secondRead != null && secondRead.expireAt > now) {
                writeLocalCache(bizKey, secondRead.data, now);
                circuitBreaker.recordSuccess();
                return build(true, "SINGLE_FLIGHT_WAIT", "等待中的线程复用已重建缓存，避免数据库并发回源", secondRead.data,
                        false, circuitBreaker.isOpen(), start, bizKey, clientId);
            }

            String dbValue = loadFromDatabase(bizKey);
            if (dbValue == null) {
                circuitBreaker.recordFailure();
                return degradeResponse("DB_MISS_DEGRADE", "数据库无数据，返回降级结果", start, bizKey, clientId);
            }

            if (redisAvailable) {
                writeRedisWithRandomTtl(bizKey, dbValue, now);
            }
            writeLocalCache(bizKey, dbValue, now);
            circuitBreaker.recordSuccess();
            return build(true, "DB_REBUILD", "数据库回源成功，已按随机TTL重建缓存", dbValue,
                    false, circuitBreaker.isOpen(), start, bizKey, clientId);
        }
    }

    /**
     * 异步续期：当缓存临近过期时提前刷新，降低同一时间窗口的大面积过期概率。
     */
    private void tryAsyncRenew(String bizKey, CacheEntry entry, long now, String source) {
        long remain = entry.expireAt - now;
        if (remain > RENEW_THRESHOLD_MILLIS) {
            return;
        }

        AtomicBoolean flag = renewFlags.computeIfAbsent(bizKey, k -> new AtomicBoolean(false));
        if (!flag.compareAndSet(false, true)) {
            return;
        }

        new Thread(() -> {
            try {
                String dbValue = loadFromDatabase(bizKey);
                if (dbValue != null) {
                    long ts = Instant.now().toEpochMilli();
                    writeRedisWithRandomTtl(bizKey, dbValue, ts);
                    writeLocalCache(bizKey, dbValue, ts);
                    log.info("cache-avalanche async-renew success, bizKey={}, source={}", bizKey, source);
                }
            } catch (Exception ex) {
                log.warn("cache-avalanche async-renew failed, bizKey={}, source={}, reason={}",
                        bizKey, source, ex.getMessage());
            } finally {
                flag.set(false);
            }
        }, "cache-avalanche-renew-" + bizKey).start();
    }

    /**
     * 写入Redis缓存并附加随机TTL抖动。
     */
    private void writeRedisWithRandomTtl(String bizKey, String data, long now) {
        long jitter = random.nextInt(TTL_RANDOM_BOUND_MILLIS + 1);
        long expireAt = now + REDIS_CACHE_BASE_TTL_MILLIS + jitter;
        redisCache.put(bizKey, new CacheEntry(data, expireAt));
    }

    /**
     * 写入本地缓存。
     */
    private void writeLocalCache(String bizKey, String data, long now) {
        long jitter = random.nextInt(500);
        localCache.put(bizKey, new CacheEntry(data, now + LOCAL_CACHE_BASE_TTL_MILLIS + jitter));
    }

    /**
     * 模拟数据库查询。
     */
    private String loadFromDatabase(String bizKey) {
        return database.computeIfPresent(bizKey, (k, oldVal) -> "商品缓存数据:" + k + "@" + Instant.now().toEpochMilli());
    }

    /**
     * 参数校验。
     */
    private boolean isValidBizKey(String bizKey) {
        return bizKey != null && bizKey.matches("^[a-zA-Z0-9_-]{3,64}$");
    }

    /**
     * 固定窗口限流。
     */
    private boolean allow(String clientId) {
        long now = Instant.now().toEpochMilli();
        long currentWindow = now / 1000;
        CounterWindow counterWindow = counters.computeIfAbsent(clientId, key -> new CounterWindow(currentWindow));
        synchronized (counterWindow) {
            if (counterWindow.window != currentWindow) {
                counterWindow.window = currentWindow;
                counterWindow.count.set(0);
            }
            return counterWindow.count.incrementAndGet() <= RATE_LIMIT_PER_SECOND;
        }
    }

    /**
     * 构建降级响应。
     */
    private CacheAvalancheQueryResponse degradeResponse(String stage, String message,
                                                        long start, String bizKey, String clientId) {
        String fallback = degradeFallback.getOrDefault(bizKey, "系统繁忙，请稍后重试");
        return build(true, stage, message, fallback, true,
                circuitBreaker.isOpen(), start, bizKey, clientId);
    }

    /**
     * 统一响应封装与日志记录。
     */
    private CacheAvalancheQueryResponse build(boolean success, String stage, String message, String data,
                                              boolean degraded, boolean circuitOpen,
                                              long start, String bizKey, String clientId) {
        long cost = System.currentTimeMillis() - start;
        log.info("cache-avalanche-query stage={}, bizKey={}, clientId={}, degraded={}, circuitOpen={}, costMs={}, message={}",
                stage, bizKey, clientId, degraded, circuitOpen, cost, message);
        CacheAvalancheQueryResponse response = new CacheAvalancheQueryResponse();
        response.setSuccess(success);
        response.setStage(stage);
        response.setMessage(message);
        response.setData(data);
        response.setDegraded(degraded);
        response.setCircuitOpen(circuitOpen);
        return response;
    }

    /**
     * 缓存实体。
     * <p>
     * 字段语义：
     * 1. data：缓存值。
     * 2. expireAt：过期时间戳（毫秒）。
     */
    private static class CacheEntry {
        private final String data;
        private final long expireAt;

        private CacheEntry(String data, long expireAt) {
            this.data = data;
            this.expireAt = expireAt;
        }
    }

    /**
     * 限流时间窗口实体。
     */
    private static class CounterWindow {
        private long window;
        private final AtomicInteger count = new AtomicInteger(0);

        private CounterWindow(long window) {
            this.window = window;
        }
    }

    /**
     * 熔断器实体。
     * <p>
     * 策略说明：
     * 1. 连续失败达到阈值后打开熔断。
     * 2. 熔断窗口内不再尝试访问下游，直接走降级。
     * 3. 成功请求会重置连续失败计数，并在窗口过后恢复闭合状态。
     */
    private static class CircuitBreaker {
        private int consecutiveFailure;
        private long openUntil;

        private synchronized void recordSuccess() {
            consecutiveFailure = 0;
            if (openUntil < Instant.now().toEpochMilli()) {
                openUntil = 0L;
            }
        }

        private synchronized void recordFailure() {
            consecutiveFailure++;
            if (consecutiveFailure >= CIRCUIT_OPEN_THRESHOLD) {
                openUntil = Instant.now().toEpochMilli() + CIRCUIT_OPEN_DURATION_MILLIS;
            }
        }

        private synchronized boolean isOpen() {
            long now = Instant.now().toEpochMilli();
            if (openUntil > now) {
                return true;
            }
            if (openUntil != 0L) {
                openUntil = 0L;
                consecutiveFailure = 0;
            }
            return false;
        }
    }

    /**
     * Redis集群高可用模拟实体。
     * <p>
     * 设计目标：
     * 1. 用多个节点模拟“哨兵/集群”的可用性思路。
     * 2. 当至。少一个节点可用时认为缓存层仍可提供服务
     * 3. 请求可通过 simulateRedisDown 强制模拟整层不可用场景。
     */
    private static class RedisClusterMock {
        private final Map<String, Boolean> nodeStatus = new ConcurrentHashMap<>();

        private RedisClusterMock() {
            nodeStatus.put("redis-node-a", true);
            nodeStatus.put("redis-node-b", true);
            nodeStatus.put("redis-node-c", true);
        }

        private boolean isAvailable(boolean requestAllow) {
            if (!requestAllow) {
                return false;
            }
            for (Boolean status : nodeStatus.values()) {
                if (Boolean.TRUE.equals(status)) {
                    return true;
                }
            }
            return false;
        }
    }
}

