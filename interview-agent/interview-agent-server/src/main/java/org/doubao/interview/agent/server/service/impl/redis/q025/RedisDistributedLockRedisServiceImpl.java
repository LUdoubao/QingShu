package org.doubao.interview.agent.server.service.impl.redis.q025;

import org.doubao.interview.agent.api.dto.redis.q025.RedisDistributedLockRequest;
import org.doubao.interview.agent.api.dto.redis.q025.RedisDistributedLockResponse;
import org.doubao.interview.agent.api.service.redis.q025.RedisDistributedLockRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 问题 025:Redis 版本分布式锁实现。
 *
 * 【核心原理】
 * 基于 Redis 的 SETNX + EXPIRE 命令组合，实现跨进程/跨节点的分布式互斥锁。
 *
 * 【关键技术点】
 * 1. 加锁：使用 Redis 原子命令 SET key value NX PX ttl
 *    - NX (Not eXists): 只有 key 不存在时才设置，保证互斥性
 *    - PX ttl: 设置毫秒级过期时间，防止死锁（客户端崩溃后锁自动释放）
 *    - value: 包含 clientId + UUID，用于解锁时验证锁的归属权
 *
 * 2. 解锁：使用 Lua 脚本保证原子性
 *    - 先比对 value 是否匹配（确认锁的持有者）
 *    - 匹配才删除，避免误删其他客户端的锁
 *    - Lua 脚本在 Redis 中是原子执行，避免检查与删除之间的并发问题
 *
 * 3. Watchdog(看门狗) 机制：解决业务超时场景
 *    - 当业务执行时间 > 锁 TTL 时，启动后台线程定期续期
 *    - 每 ttl/3 的时间间隔检查并刷新过期时间
 *    - 业务完成后自动停止，防止锁永远不释放
 *
 * 【与内存锁的区别】
 * - 内存锁：单机 JVM 内有效，无法跨进程/跨节点
 * - Redis 锁：支持分布式集群环境，多实例共享同一份锁状态
 */
@Service
public class RedisDistributedLockRedisServiceImpl implements RedisDistributedLockRedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLockRedisServiceImpl.class);

    private static final String LOCK_PREFIX = "q025:lock:";

    private static final String LUA_UNLOCK =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) else return 0 end";

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLockRedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Redis 分布式锁主流程。
     *
     * 【执行步骤】
     * 1. 参数校验：检查 lockKey、clientId、TTL、业务时间是否合法
     * 2. 尝试加锁：调用 SETNX 命令，失败则直接返回
     * 3. 判断是否需要 Watchdog：
     *    - enableWatchdog=true 且 业务时间 > TTL 时启动
     *    - Watchdog 会在后台定期续期，防止业务未完成锁已过期
     * 4. 执行业务逻辑：模拟真实业务操作（sleep workMillis）
     * 5. 解锁：无论业务成功或失败，都在 finally 块中确保解锁
     * 6. 返回结果：包含加锁/解锁状态、Watchdog 是否生效等信息
     */
    public RedisDistributedLockResponse runRedis(RedisDistributedLockRequest request) {
        // ========== Step 1: 参数校验 ==========
        String lockKey = request == null ? null : request.getLockKey();
        String clientId = request == null ? null : request.getClientId();
        long ttl = request == null ? 0 : request.getLockTtlMillis();
        long workMillis = request == null ? 0 : request.getBizWorkMillis();
        boolean enableWatchdog = request != null && request.isEnableWatchdog();
        
        if (!isValid(lockKey, clientId, ttl, workMillis)) {
            return build(false, "PARAM_VALIDATION", "参数非法", null, false, false);
        }
        
        // ========== Step 2: 构造 Redis Key 和唯一令牌 ==========
        // Redis Key 格式：q025:lock:{bizKey}，便于分类管理
        String redisKey = LOCK_PREFIX + lockKey;
        // 唯一令牌：clientId + UUID，用于后续解锁时的身份验证
        // 作用：防止 A 客户端误删 B 客户端持有的锁
        String ownerToken = clientId + "-" + UUID.randomUUID().toString();
        
        // ========== Step 3: 尝试加锁 ==========
        // 调用 Redis SETNX 命令，只有 key 不存在时才设置成功
        boolean locked = setNxPx(redisKey, ownerToken, ttl);
        if (!locked) {
            // 加锁失败：说明锁已被其他客户端持有
            return build(false, "LOCK_NOT_ACQUIRED", "Redis 锁已被占用", ownerToken, false, false);
        }
        
        // ========== Step 4: 准备 Watchdog 机制 ==========
        AtomicBoolean watchdogRunning = new AtomicBoolean(false);
        boolean renewed = false;
        boolean unlocked;
        
        // ========== Step 5: 执行业务逻辑 ==========
        try {
            // 判断是否需要启动 Watchdog:
            // - enableWatchdog=true: 用户明确要求启用
            // - workMillis > ttl: 业务执行时间超过锁 TTL，需要续期保护
            if (enableWatchdog && workMillis > ttl) {
                renewed = startWatchdog(redisKey, ownerToken, ttl, watchdogRunning);
            }
            simulateBusinessWork(workMillis);
        } finally {
            // ========== Step 6: 清理与解锁 ==========
            // 无论业务成功或异常，都必须执行以下清理操作
                    
            // 6.1 停止 Watchdog
            watchdogRunning.set(false);
                    
            // 6.2 执行解锁：通过 Lua 脚本安全删除锁
            unlocked = unlockByLua(redisKey, ownerToken);
                    
            // 记录日志：追踪锁的生命周期
            log.info("q025 redis unlock, key={}, token={}, unlocked={}", redisKey, ownerToken, unlocked);
        }
        
        // ========== Step 7: 构建响应 ==========
        String stage = unlocked ? "REDIS_LOCK_DONE" : "REDIS_UNLOCK_FAILED";
        String message = unlocked 
                ? "Redis 加锁/解锁流程完成" 
                : "业务结束，但 Lua 未删除锁（可能已过期或归属变更）";
        return build(unlocked, stage, message, ownerToken, renewed, unlocked);
    }

    /**
     * 使用 Redis SETNX 命令实现加锁。
     *
     * 【对应 Redis 命令】
     * SET key value NX PX ttl
     * 
     * 【为什么不用单独的 SETNX + EXPIRE？】
     * - 分两步执行会出现原子性问题：SETNX 成功后、EXPIRE 前客户端崩溃
     * - 导致锁变成永不过期的死锁，必须等待手动清理
     * - Redis 2.6.12+ 支持 SET 命令的扩展选项，可以原子地完成两个动作
     */
    private boolean setNxPx(String key, String value, long ttlMillis) {
        // 使用 Spring Data Redis 执行原子命令
        // 底层调用：SET key value NX PX ttlMillis
        Boolean result = redisTemplate.execute((RedisConnection connection) -> connection.set(
                key.getBytes(StandardCharsets.UTF_8),           // Key 字节数组
                value.getBytes(StandardCharsets.UTF_8),         // Value 字节数组
                Expiration.milliseconds(ttlMillis),             // 过期时间（毫秒）
                RedisStringCommands.SetOption.SET_IF_ABSENT     // NX 选项：仅当 key 不存在时设置
        ));
        return Boolean.TRUE.equals(result);
    }

    /**
     * 使用 Lua 脚本安全解锁。
     *
     * 【为什么用 Lua 而不用 get + del？】
     * - 普通方式存在并发窗口：
     *   1. 客户端 A 执行 get，发现 value 匹配
     *   2. 与此同时，锁过期了
     *   3. 客户端 B 立即获取到同一把锁
     *   4. 客户端 A 执行 del，误删了 B 的锁
     * 
     * - Lua 脚本的优势：
     *   Redis 会将整个 Lua 脚本作为单个原子命令执行
     *   在脚本执行期间，不会插入其他客户端的命令
     *   彻底消除"检查"与"删除"之间的并发窗口
     *
     * 【Lua 脚本逻辑】
     * if redis.call('get', KEYS[1]) == ARGV[1] then
     *     return redis.call('del', KEYS[1])
     * else
     *     return 0
     * end
     */
    private boolean unlockByLua(String key, String value) {
        // 执行 Lua 脚本
        Long result = redisTemplate.execute((RedisConnection connection) -> connection.eval(
                LUA_UNLOCK.getBytes(StandardCharsets.UTF_8),    // Lua 脚本字节数组
                ReturnType.INTEGER,                              // 返回值类型：整数
                1,                                               // KEYS 的数量
                key.getBytes(StandardCharsets.UTF_8),            // KEYS[1]: 锁 key
                value.getBytes(StandardCharsets.UTF_8)           // ARGV[1]: 令牌
        ));
        // 返回值 > 0 表示成功删除
        return result != null && result > 0;
    }

    /**
     * 启动 Watchdog(看门狗) 后台线程，用于业务超时场景的锁续期。
     *
     * 【使用场景】
     * 当业务执行时间 > 锁 TTL 时，如果不续期，锁会在业务完成前就过期，
     * 导致其他客户端获取到锁，破坏互斥性。
     * 
     * 【工作原理】
     * 1. 启动后台守护线程，定期检查锁状态
     * 2. 检查间隔：ttl / 3（至少 50ms），平衡性能与安全性
     * 3. 每次检查:
     *    - 确认锁的 value 仍匹配（防止误给别人的锁续期）
     *    - 刷新过期时间为 ttl
     * 4. 退出条件:
     *    - running 标志被设为 false（业务完成）
     *    - 锁的 value 不匹配（锁已释放或被别人抢走）
     *    - Redis 异常（网络断开等）
     */
    private boolean startWatchdog(String key, String token, long ttlMillis, AtomicBoolean running) {
        // 防止重复启动（CAS 原子操作）
        if (!running.compareAndSet(false, true)) {
            return true;  // 已在运行，直接返回成功
        }
        
        // 计算续期间隔：ttl 的 1/3，但至少 50ms
        long interval = Math.max(50L, ttlMillis / 3);
        
        // 创建并启动后台线程
        Thread t = new Thread(() -> {
            while (running.get()) {
                try {
                    // 休眠指定间隔
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    // 线程被中断：恢复中断标志并退出
                    Thread.currentThread().interrupt();
                    running.set(false);
                    break;
                }
                
                try {
                    // Step 1: 获取当前锁的持有者
                    String owner = redisTemplate.opsForValue().get(key);
                    
                    // Step 2: 验证是否仍是当前客户端的锁
                    if (!token.equals(owner)) {
                        // 锁已过期或被别人抢走，不再续期
                        running.set(false);
                        break;
                    }
                    
                    // Step 3: 刷新过期时间
                    redisTemplate.expire(key, java.time.Duration.ofMillis(ttlMillis));
                    
                } catch (DataAccessException ex) {
                    // Redis 连接异常，安全退出
                    running.set(false);
                    break;
                }
            }
        }, "q025-redis-watchdog-" + key);
        
        // 设置为守护线程：JVM 退出时自动终止
        t.setDaemon(true);
        t.start();
        return true;
    }

    private void simulateBusinessWork(long workMillis) {
        try {
            Thread.sleep(workMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isValid(String lockKey, String clientId, long ttl, long workMillis) {
        return lockKey != null && !lockKey.trim().isEmpty()
                && clientId != null && !clientId.trim().isEmpty()
                && ttl > 0 && workMillis > 0;
    }

    private RedisDistributedLockResponse build(boolean success, String stage, String message,
                                               String ownerToken, boolean renewed, boolean unlocked) {
        RedisDistributedLockResponse response = new RedisDistributedLockResponse();
        response.setSuccess(success);
        response.setStage(stage);
        response.setMessage(message + " @" + Instant.now().toEpochMilli());
        response.setLockOwnerToken(ownerToken);
        response.setWatchdogRenewed(renewed);
        response.setUnlockedByLua(unlocked);
        response.setIdempotentHint(true);
        return response;
    }
}
