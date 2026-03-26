package org.doubao.interview.agent.server.service.impl;

import org.doubao.interview.agent.api.dto.CachePenetrationCheckRequest;
import org.doubao.interview.agent.api.dto.CachePenetrationCheckResponse;
import org.doubao.interview.agent.api.service.CachePenetrationGovernanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 问题008：缓存穿透治理示例实现。
 *
 * 这是一个“可运行、可观察、便于讲解”的教学实现，按真实系统常见顺序串联四种治理手段：
 * 1. 参数校验：无效请求尽早失败，减少下游资源消耗。
 * 2. 限流：在入口处保护系统，避免异常流量压垮缓存和数据库。
 * 3. 空值缓存：数据库不存在时写入短TTL，防止相同key重复穿透。
 * 4. 布隆过滤器：在访问缓存/数据库前快速判断“高度疑似不存在”的key。
 *
 * 说明：为保证最小可运行，本类使用内存结构模拟缓存与数据库，生产环境可替换为 Redis/MySQL。
 */
@Service
public class CachePenetrationGovernanceServiceImpl implements CachePenetrationGovernanceService {

    private static final Logger log = LoggerFactory.getLogger(CachePenetrationGovernanceServiceImpl.class);

    /** 空值缓存TTL：30秒。避免长期缓存“无数据”导致数据延迟可见。 */
    private static final long EMPTY_CACHE_TTL_MILLIS = 30_000L;
    /** 正常值缓存TTL：5分钟。演示场景下取固定值。 */
    private static final long VALUE_CACHE_TTL_MILLIS = 300_000L;
    /** 固定窗口限流阈值：每个client每秒最多5次请求。 */
    private static final int RATE_LIMIT_PER_SECOND = 5;

    /** 模拟数据库。key=dataId, value=业务数据。 */
    private final Map<String, String> database = new ConcurrentHashMap<String, String>();
    /** 模拟缓存。统一存储正常值与空值占位。 */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();
    /** 限流计数器。key=clientId。 */
    private final Map<String, CounterWindow> counters = new ConcurrentHashMap<String, CounterWindow>();
    /** 布隆过滤器：预热可用key，拦截明显不存在的key。 */
    private final SimpleBloomFilter bloomFilter = new SimpleBloomFilter(1 << 20);

    /**
     * 初始化模拟数据。
     *
     * 预置 item-1 ~ item-1000，表示“数据库真实存在的数据集合”，
     * 并同步写入布隆过滤器，保证布隆过滤器与数据集口径一致。
     */
    @PostConstruct
    public void init() {
        for (int i = 1; i <= 1000; i++) {
            String dataId = "item-" + i;
            database.put(dataId, "模拟数据库内容:" + dataId);
            bloomFilter.add(dataId);
        }
    }

    /**
     * 缓存穿透治理主流程。
     *
     * 流程解释：
     * 1) 参数非法直接拒绝（PARAM_VALIDATION）。
     * 2) 限流不通过直接拒绝（RATE_LIMIT）。
     * 3) 缓存命中：命中正常值返回 CACHE_HIT；命中空值返回 EMPTY_CACHE。
     * 4) 缓存未命中时走布隆：若判定不存在，写入空值缓存并返回 BLOOM_FILTER。
     * 5) 布隆可能存在则查库：不存在写空值缓存(DB_EMPTY)，存在写正常缓存(DB_HIT)。
     */
    @Override
    public CachePenetrationCheckResponse check(CachePenetrationCheckRequest request) {
        long start = System.currentTimeMillis();
        String dataId = request == null ? null : request.getDataId();
        String clientId = request == null ? null : request.getClientId();

        if (!isValidDataId(dataId)) {
            return build(false, "PARAM_VALIDATION", "非法参数，已前置拦截", null, start, dataId, clientId);
        }

        if (!allow(clientId == null ? "unknown" : clientId)) {
            return build(false, "RATE_LIMIT", "请求过于频繁，已限流", null, start, dataId, clientId);
        }

        CacheEntry cacheEntry = cache.get(dataId);
        long now = Instant.now().toEpochMilli();
        if (cacheEntry != null && cacheEntry.expireAt > now) {
            if (cacheEntry.emptyValue) {
                return build(true, "EMPTY_CACHE", "命中空值缓存，避免重复穿透", null, start, dataId, clientId);
            }
            return build(true, "CACHE_HIT", "命中缓存", cacheEntry.value, start, dataId, clientId);
        }

        if (!bloomFilter.mightContain(dataId)) {
            // 布隆判定“不存在概率极高”，直接写入短TTL空值，挡住后续同key流量。
            cache.put(dataId, CacheEntry.empty(now + EMPTY_CACHE_TTL_MILLIS));
            return build(true, "BLOOM_FILTER", "布隆过滤器判定大概率不存在，已写入空值缓存", null, start, dataId, clientId);
        }

        String dbValue = database.get(dataId);
        if (dbValue == null) {
            // 布隆存在误判概率：即使“可能存在”，查库仍可能为空，因此仍需空值缓存兜底。
            cache.put(dataId, CacheEntry.empty(now + EMPTY_CACHE_TTL_MILLIS));
            return build(true, "DB_EMPTY", "数据库不存在，已写入短TTL空值缓存", null, start, dataId, clientId);
        }

        cache.put(dataId, CacheEntry.value(dbValue, now + VALUE_CACHE_TTL_MILLIS));
        return build(true, "DB_HIT", "查询数据库成功并写入缓存", dbValue, start, dataId, clientId);
    }

    /**
     * 统一构建响应并打印上下文日志。
     */
    private CachePenetrationCheckResponse build(boolean allowed, String stage, String message,
                                                String data, long start, String dataId, String clientId) {
        long cost = System.currentTimeMillis() - start;
        log.info("cache-penetration-check stage={}, dataId={}, clientId={}, costMs={}, message={}",
                stage, dataId, clientId, cost, message);
        CachePenetrationCheckResponse response = new CachePenetrationCheckResponse();
        response.setAllowed(allowed);
        response.setStage(stage);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 参数校验：限制字符范围和长度，避免无效/恶意请求直接打到后续链路。
     */
    private boolean isValidDataId(String dataId) {
        return dataId != null && dataId.matches("^[a-zA-Z0-9_-]{3,64}$");
    }

    /**
     * 固定窗口限流。
     *
     * 并发说明：
     * 1. counters 使用 ConcurrentHashMap 保证并发读写安全。
     * 2. 每个 clientId 对应一个 CounterWindow，并在该对象上加锁，
     *    避免同一 client 并发更新时出现计数不准。
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
     * 缓存实体：统一表达“正常值缓存”和“空值缓存”。
     */
    private static class CacheEntry {
        /** true 表示这是空值占位记录，不代表真实数据。 */
        private final boolean emptyValue;
        /** 真实缓存值；当 emptyValue=true 时该字段为空。 */
        private final String value;
        /** 过期时间戳（毫秒）。 */
        private final long expireAt;

        private CacheEntry(boolean emptyValue, String value, long expireAt) {
            this.emptyValue = emptyValue;
            this.value = value;
            this.expireAt = expireAt;
        }

        private static CacheEntry empty(long expireAt) {
            return new CacheEntry(true, null, expireAt);
        }

        private static CacheEntry value(String value, long expireAt) {
            return new CacheEntry(false, value, expireAt);
        }
    }

    /**
     * 限流窗口实体：记录某个 clientId 在当前秒窗口内的请求次数。
     */
    private static class CounterWindow {
        /** 秒级窗口编号：epochMillis / 1000。 */
        private long window;
        /** 当前窗口请求计数。 */
        private final AtomicInteger count = new AtomicInteger(0);

        private CounterWindow(long window) {
            this.window = window;
        }
    }

    /**
     * 简化版布隆过滤器
     *
     * 设计取舍：
     * 1. 使用3个哈希种子，降低误判率。
     * 2. 只允许“可能存在”或“肯定不存在”两类结果。
     * 3. 位图大小固定为2的幂，便于位运算取模。
     */
    private static class SimpleBloomFilter {
        private final BitSet bits;
        private final int size;

        private SimpleBloomFilter(int size) {
            this.size = size;
            this.bits = new BitSet(size);
        }

        /**
         * 将值写入布隆过滤器。
         */
        private void add(String value) {
            bits.set(index(value, 17));
            bits.set(index(value, 31));
            bits.set(index(value, 131));
        }

        /**
         * 判断值是否“可能存在”。
         * 返回 false 时可认为“肯定不存在”；返回 true 时仍需要下游校验。
         */
        private boolean mightContain(String value) {
            return bits.get(index(value, 17))
                    && bits.get(index(value, 31))
                    && bits.get(index(value, 131));
        }

        /**
         * 基于种子计算位图下标。
         */
        private int index(String value, int seed) {
            int hash = 0;
            for (int i = 0; i < value.length(); i++) {
                hash = hash * seed + value.charAt(i);
            }
            return (size - 1) & hash;
        }
    }
}