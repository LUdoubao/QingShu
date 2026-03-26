package org.doubao.interview.agent.server.service.impl;

import org.doubao.interview.agent.api.dto.CacheBreakdownQueryRequest;
import org.doubao.interview.agent.api.dto.CacheBreakdownQueryResponse;
import org.doubao.interview.agent.api.service.CacheBreakdownGovernanceService;
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
 * 问题 009：缓存击穿治理示例实现。
 *
 * 目标：演示“热点 key 在失效瞬间被并发访问”时的常见治理组合。
 *
 * 设计点：
 * 1. 互斥锁/单飞：同一时刻仅一个线程回源重建缓存，避免数据库被并发打穿。
 * 2. 逻辑过期：命中过期数据时先返回旧值，再异步刷新，保障读请求低延迟。
 * 3. 极热 key 不过期：通过主动异步更新代替被动过期，避免失效瞬间抖动。
 * 4. 本地缓存 + 限流：在入口和节点内继续削峰，提升整体稳定性。
 *
 * 说明：当前使用内存结构模拟 DB 和缓存，便于最小可运行；生产环境可替换 Redis/MySQL。
 */
@Service
public class CacheBreakdownGovernanceServiceImpl implements CacheBreakdownGovernanceService {

    private static final Logger log = LoggerFactory.getLogger(CacheBreakdownGovernanceServiceImpl.class);

    /** 物理过期策略（互斥重建）的缓存TTL。 */
    private static final long MUTEX_TTL_MILLIS = 20_000L;
    /** 逻辑过期策略的逻辑TTL。 */
    private static final long LOGICAL_EXPIRE_MILLIS = 12_000L;
    /** 本地缓存短TTL，用于快速削峰。 */
    private static final long LOCAL_CACHE_MILLIS = 3_000L;
    /** 限流阈值：每 client 每秒最多 8 次请求。 */
    private static final int RATE_LIMIT_PER_SECOND = 8;

    /** 模拟数据库。 */
    private final Map<String, String> database = new ConcurrentHashMap<String, String>();
    /** 热点 key 与治理策略映射。 */
    private final Map<String, HotKeyPolicy> policies = new ConcurrentHashMap<String, HotKeyPolicy>();
    /** 模拟远端缓存（可类比 Redis）。 */
    private final Map<String, CacheEntry> remoteCache = new ConcurrentHashMap<String, CacheEntry>();
    /** 模拟本地缓存（进程内缓存）。 */
    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<String, CacheEntry>();
    /** 限流计数窗口。 */
    private final Map<String, CounterWindow> counters = new ConcurrentHashMap<String, CounterWindow>();
    /** 每个 hotKey 对应一把锁，用于单飞重建。 */
    private final Map<String, Object> keyLocks = new ConcurrentHashMap<String, Object>();

    /**
     * 初始化样例数据与策略。
     */
    @PostConstruct
    public void init() {
        database.put("hot-mutex", "热点数据:hot-mutex@" + Instant.now().toEpochMilli());
        database.put("hot-logical", "热点数据:hot-logical@" + Instant.now().toEpochMilli());
        database.put("hot-never-expire", "热点数据:hot-never-expire@" + Instant.now().toEpochMilli());

        policies.put("hot-mutex", HotKeyPolicy.mutex(MUTEX_TTL_MILLIS));
        policies.put("hot-logical", HotKeyPolicy.logicalExpire(LOGICAL_EXPIRE_MILLIS));
        policies.put("hot-never-expire", HotKeyPolicy.neverExpire());

        for (Map.Entry<String, HotKeyPolicy> entry : policies.entrySet()) {
            String hotKey = entry.getKey();
            HotKeyPolicy policy = entry.getValue();
            String value = database.get(hotKey);
            long now = Instant.now().toEpochMilli();
            remoteCache.put(hotKey, CacheEntry.fromPolicy(value, now, policy));
        }
    }

    /**
     * 查询主流程。
     *
     * 流程顺序：
     * 1. 参数校验。
     * 2. 限流。
     * 3. 本地缓存命中直接返回。
     * 4. 根据热点策略进入：不过期主动刷新 / 逻辑过期 / 互斥重建。
     */
    @Override
    public CacheBreakdownQueryResponse query(CacheBreakdownQueryRequest request) {
        long start = System.currentTimeMillis();
        String hotKey = request == null ? null : request.getHotKey();
        String clientId = request == null ? null : request.getClientId();

        if (!isValidHotKey(hotKey)) {
            return build(false, "PARAM_VALIDATION", "热点key非法，已前置拦截", null, start, hotKey, clientId);
        }

        if (!allow(clientId == null ? "unknown" : clientId)) {
            return build(false, "RATE_LIMIT", "请求频率过高，已限流", null, start, hotKey, clientId);
        }

        CacheEntry local = localCache.get(hotKey);
        long now = Instant.now().toEpochMilli();
        if (local != null && local.physicalExpireAt > now) {
            return build(true, "LOCAL_CACHE_HIT", "命中本地缓存，完成快速削峰", local.data, start, hotKey, clientId);
        }

        HotKeyPolicy policy = policies.get(hotKey);
        if (policy == null) {
            return build(false, "UNKNOWN_HOT_KEY", "未配置热点key策略", null, start, hotKey, clientId);
        }

        if (policy.neverExpire) {
            return queryNeverExpire(hotKey, policy, start, clientId);
        }

        CacheEntry remote = remoteCache.get(hotKey);
        if (remote == null) {
            return rebuildBySingleFlight(hotKey, policy, start, clientId, "REMOTE_CACHE_EMPTY");
        }

        if (policy.logicalExpire) {
            return queryByLogicalExpire(hotKey, policy, remote, start, clientId);
        }

        if (remote.physicalExpireAt > now) {
            writeLocalCache(hotKey, remote.data, now);
            return build(true, "REMOTE_CACHE_HIT", "命中远端缓存", remote.data, start, hotKey, clientId);
        }

        return rebuildBySingleFlight(hotKey, policy, start, clientId, "REMOTE_CACHE_EXPIRED");
    }

    /**
     * 不过期策略：始终返回缓存值，并在到达刷新阈值后触发异步主动更新。
     */
    private CacheBreakdownQueryResponse queryNeverExpire(String hotKey, HotKeyPolicy policy, long start, String clientId) {
        long now = Instant.now().toEpochMilli();
        CacheEntry remote = remoteCache.get(hotKey);
        if (remote == null) {
            return rebuildBySingleFlight(hotKey, policy, start, clientId, "NO_EXPIRE_FIRST_BUILD");
        }

        if (remote.nextActiveRefreshAt <= now && remote.activeRefreshRunning.compareAndSet(false, true)) {
            new Thread(() -> {
                try {
                    rebuildFromDatabase(hotKey, policy, "主动刷新");
                } finally {
                    CacheEntry entry = remoteCache.get(hotKey);
                    if (entry != null) {
                        entry.activeRefreshRunning.set(false);
                    }
                }
            }, "cache-breakdown-active-refresh-" + hotKey).start();
        }

        writeLocalCache(hotKey, remote.data, now);
        return build(true, "NO_EXPIRE_ACTIVE_UPDATE", "热点数据不过期，按阈值主动异步更新", remote.data, start, hotKey, clientId);
    }

    /**
     * 逻辑过期策略：逻辑未过期直接返回；逻辑过期则返回旧值并异步刷新。
     */
    private CacheBreakdownQueryResponse queryByLogicalExpire(String hotKey, HotKeyPolicy policy, CacheEntry remote,
                                                             long start, String clientId) {
        long now = Instant.now().toEpochMilli();
        writeLocalCache(hotKey, remote.data, now);
        if (remote.logicalExpireAt > now) {
            return build(true, "LOGICAL_EXPIRE_HIT", "逻辑未过期，直接返回缓存", remote.data, start, hotKey, clientId);
        }

        if (remote.activeRefreshRunning.compareAndSet(false, true)) {
            new Thread(() -> {
                try {
                    rebuildFromDatabase(hotKey, policy, "逻辑过期异步刷新");
                } finally {
                    CacheEntry refreshed = remoteCache.get(hotKey);
                    if (refreshed != null) {
                        refreshed.activeRefreshRunning.set(false);
                    }
                }
            }, "cache-breakdown-logical-refresh-" + hotKey).start();
        }
        return build(true, "LOGICAL_EXPIRE_STALE", "逻辑已过期，返回旧值并触发异步刷新", remote.data, start, hotKey, clientId);
    }

    /**
     * 互斥锁/单飞重建。
     *
     * 【核心思想】
     * 当缓存物理过期后，多个并发请求同时访问该热点 key 时，通过互斥锁机制保证只有一个线程回源数据库，
     * 其他线程等待锁释放后复用已重建的缓存，从而避免大量并发请求直接打到数据库上导致缓存击穿。
     *
     * 【关键流程】
     * 1. 获取热点 key 对应的专属锁（每个 hotKey 一把锁，减少锁竞争）。
     * 2. 进入 synchronized 块后执行二次检查（Double-Check），防止重复回源：
     *    - 检查当前缓存是否已被其他线程重建完成
     *    - 若缓存可用（未过期或逻辑未过期），直接复用结果，无需再次查询数据库
     * 3. 若缓存确实需要重建，则当前线程执行回源操作：
     *    - 查询数据库获取最新数据
     *    - 更新远端缓存和本地缓存
     *    - 设置新的过期时间
     * 4. 释放锁，其他等待的线程可通过二次检查复用新缓存。
     *
     * 【为什么需要二次检查？】
     * - 场景：线程 A 获得锁并开始重建，线程 B 和 C 在锁外等待
     * - 过程：A 重建完成后释放锁 → B 获得锁 → 此时缓存已由 A 重建好
     * - 优化：B 通过二次检查发现缓存可用，直接复用，避免重复查询数据库
     * - 收益：将 N 次数据库请求合并为 1 次，极大减轻数据库压力
     *
     * 【锁粒度设计】
     * - 每个热点 key 独立一把锁，而不是全局一把大锁
     * - 好处：不同 key 的重建操作可以并行执行，互不阻塞
     * - 示例：hot-mutex 和 hot-logical 可以同时重建，不需要互相等待
     *
     * 【适用场景】
     * - 缓存物理过期后的首次访问
     * - 对数据一致性要求较高，不能容忍返回旧值的场景
     * - 数据库能够承受单次查询压力，但无法应对并发冲击的场景
     *
     * 【性能权衡】
     * - 优点：彻底保护数据库，避免并发击穿；保证数据一致性
     * - 缺点：首个请求需要等待数据库查询（相比逻辑过期方案延迟更高）
     * - 建议：与逻辑过期方案配合使用，普通数据用互斥锁，极热数据用逻辑过期
     *
     * @param hotKey   热点 key
     * @param policy   热点策略配置
     * @param start    请求开始时间戳（用于计算耗时）
     * @param clientId 客户端标识（用于日志追踪）
     * @param reason   触发重建的原因（如 REMOTE_CACHE_EXPIRED）
     * @return 查询响应（包含重建结果和阶段标识）
     */
    private CacheBreakdownQueryResponse rebuildBySingleFlight(String hotKey, HotKeyPolicy policy,
                                                              long start, String clientId, String reason) {
        Object lock = keyLocks.computeIfAbsent(hotKey, k -> new Object());
        synchronized (lock) {
            long now = Instant.now().toEpochMilli();
            CacheEntry secondRead = remoteCache.get(hotKey);
            if (secondRead != null) {
                boolean canUse = policy.neverExpire || secondRead.physicalExpireAt > now ||
                        (policy.logicalExpire && secondRead.logicalExpireAt > now);
                if (canUse) {
                    writeLocalCache(hotKey, secondRead.data, now);
                    return build(true, "SINGLE_FLIGHT_WAIT", "当前线程复用已重建缓存，避免并发击穿", secondRead.data,
                            start, hotKey, clientId);
                }
            }

            String rebuilt = rebuildFromDatabase(hotKey, policy, reason);
            writeLocalCache(hotKey, rebuilt, now);
            return build(true, "SINGLE_FLIGHT_REBUILD", "互斥锁/单飞生效，仅一个线程回源重建", rebuilt,
                    start, hotKey, clientId);
        }
    }

    /**
     * 模拟回源数据库并重建远端缓存。
     */
    private String rebuildFromDatabase(String hotKey, HotKeyPolicy policy, String reason) {
        String value = database.computeIfPresent(hotKey, (k, oldVal) -> "热点数据:" + k + "@" + Instant.now().toEpochMilli());
        long now = Instant.now().toEpochMilli();
        if (value != null) {
            remoteCache.put(hotKey, CacheEntry.fromPolicy(value, now, policy));
            log.info("cache-breakdown rebuild success, hotKey={}, reason={}", hotKey, reason);
            return value;
        }
        log.warn("cache-breakdown rebuild skipped, hotKey={}, reason={}, dbMissing=true", hotKey, reason);
        return null;
    }

    /**
     * 回写本地缓存，作为节点内短期热点缓冲层。
     */
    private void writeLocalCache(String hotKey, String data, long now) {
        localCache.put(hotKey, CacheEntry.local(data, now + LOCAL_CACHE_MILLIS));
    }

    /**
     * 热点 key 参数校验。
     */
    private boolean isValidHotKey(String hotKey) {
        return hotKey != null && hotKey.matches("^[a-zA-Z0-9_-]{3,64}$");
    }

    /**
     * 固定窗口限流。
     *
     * 并发说明：同一 clientId 的窗口对象上加锁，保证窗口切换和计数更新一致性。
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
     * 统一封装响应并记录关键日志。
     */
    private CacheBreakdownQueryResponse build(boolean success, String stage, String message,
                                              String data, long start, String hotKey, String clientId) {
        long cost = System.currentTimeMillis() - start;
        log.info("cache-breakdown-query stage={}, hotKey={}, clientId={}, costMs={}, message={}",
                stage, hotKey, clientId, cost, message);
        CacheBreakdownQueryResponse response = new CacheBreakdownQueryResponse();
        response.setSuccess(success);
        response.setStage(stage);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 热点 key 策略实体。
     */
    private static class HotKeyPolicy {
        /** true 表示启用逻辑过期策略。 */
        private final boolean logicalExpire;
        /** true 表示该 key 常驻缓存，不依赖过期驱逐。 */
        private final boolean neverExpire;
        /** 策略对应的 TTL（逻辑或物理含义由策略决定）。 */
        private final long ttlMillis;

        private HotKeyPolicy(boolean logicalExpire, boolean neverExpire, long ttlMillis) {
            this.logicalExpire = logicalExpire;
            this.neverExpire = neverExpire;
            this.ttlMillis = ttlMillis;
        }

        private static HotKeyPolicy mutex(long ttlMillis) {
            return new HotKeyPolicy(false, false, ttlMillis);
        }

        private static HotKeyPolicy logicalExpire(long ttlMillis) {
            return new HotKeyPolicy(true, false, ttlMillis);
        }

        private static HotKeyPolicy neverExpire() {
            return new HotKeyPolicy(false, true, Long.MAX_VALUE);
        }
    }

    /**
     * 缓存实体。
     *
     * 同时承载：
     * 1. 物理过期时间（physicalExpireAt）。
     * 2. 逻辑过期时间（logicalExpireAt）。
     * 3. 主动刷新时间点（nextActiveRefreshAt）。
     * 4. 刷新并发保护标记（activeRefreshRunning）。
     *
     * 【物理过期 vs 逻辑过期】
     * 
     * 物理过期时间 (physicalExpireAt)：
     * - 含义：缓存的真实生命周期终点，到达该时间后缓存数据被视为无效
     * - 行为：过期后不能使用，必须回源数据库重建缓存
     * - 并发控制：需要使用互斥锁保证只有一个线程回源（防止缓存击穿）
     * - 适用场景：普通热点数据，对数据一致性要求较高的场景
     * 
     * 逻辑过期时间 (logicalExpireAt)：
     * - 含义：业务层面的软过期标记，用于在保证低延迟的前提下触发异步刷新
     * - 行为：过期后仍可返回旧值，同时触发后台异步刷新线程
     * - 并发控制：通过 AtomicBoolean 防止重复启动刷新线程，无需阻塞当前请求
     * - 适用场景：高并发、对延迟敏感的热点数据，可容忍短暂的数据不一致
     * 
     * 【组合使用示例】
     * 假设 TTL = 12 秒，物理过期 = 20 秒，逻辑过期 = 12 秒：
     * - 0-12 秒：物理未过期 + 逻辑未过期 → 直接返回缓存
     * - 12-20 秒：物理未过期 + 逻辑已过期 → 返回旧值 + 后台异步刷新
     * - 20 秒+ ：物理已过期 + 逻辑已过期 → 互斥锁重建（单飞模式）
     * 
     * 【设计思想】
     * - 物理过期：保护数据库，防止数据永远不更新，作为最终兜底机制
     * - 逻辑过期：牺牲少量数据一致性，换取零等待的用户体验，避免缓存失效瞬间的并发冲击
     * - 组合优势：在缓存击穿场景下，既保证高性能又保证高可用性
     */
    private static class CacheEntry {
        /** 缓存数据。 */
        private final String data;
        /** 
         * 物理过期时间戳。
         * 缓存的真实失效时间，到达后必须回源重建。
         * 用于传统的缓存过期驱逐机制，是缓存有效性的最终判断标准。
         */
        private final long physicalExpireAt;
        /** 
         * 逻辑过期时间戳。
         * 业务层面的过期标记，到达后会触发异步刷新但不会立即废弃缓存。
         * 目的是保证低延迟：即使逻辑过期，仍可返回旧值，同时后台刷新。
         */
        private final long logicalExpireAt;
        /** 下一次主动刷新时间戳。 */
        private final long nextActiveRefreshAt;
        /** 是否正在异步刷新，防止重复启动刷新线程。 */
        private final AtomicBoolean activeRefreshRunning;

        private CacheEntry(String data, long physicalExpireAt, long logicalExpireAt,
                           long nextActiveRefreshAt, AtomicBoolean activeRefreshRunning) {
            this.data = data;
            this.physicalExpireAt = physicalExpireAt;
            this.logicalExpireAt = logicalExpireAt;
            this.nextActiveRefreshAt = nextActiveRefreshAt;
            this.activeRefreshRunning = activeRefreshRunning;
        }

        /**
         * 根据策略构建远端缓存实体。
         */
        private static CacheEntry fromPolicy(String data, long now, HotKeyPolicy policy) {
            long physicalExpireAt = policy.neverExpire ? Long.MAX_VALUE : now + policy.ttlMillis;
            long logicalExpireAt = policy.logicalExpire ? now + policy.ttlMillis : Long.MAX_VALUE;
            long nextActiveRefreshAt = policy.neverExpire ? now + 10_000L : Long.MAX_VALUE;
            return new CacheEntry(data, physicalExpireAt, logicalExpireAt, nextActiveRefreshAt, new AtomicBoolean(false));
        }

        /**
         * 构建本地缓存实体（只关注短物理过期）。
         */
        private static CacheEntry local(String data, long expireAt) {
            return new CacheEntry(data, expireAt, Long.MAX_VALUE, Long.MAX_VALUE, new AtomicBoolean(false));
        }
    }

    /**
     * 限流计数窗口实体。
     */
    private static class CounterWindow {
        /** 秒级时间窗口（epochMillis / 1000）。 */
        private long window;
        /** 当前窗口内请求次数。 */
        private final AtomicInteger count = new AtomicInteger(0);

        private CounterWindow(long window) {
            this.window = window;
        }
    }
}