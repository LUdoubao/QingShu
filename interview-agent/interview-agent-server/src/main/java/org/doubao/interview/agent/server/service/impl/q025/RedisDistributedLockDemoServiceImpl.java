package org.doubao.interview.agent.server.service.impl.q025;

import org.doubao.interview.agent.api.dto.q025.RedisDistributedLockRequest;
import org.doubao.interview.agent.api.dto.q025.RedisDistributedLockResponse;
import org.doubao.interview.agent.api.service.q025.RedisDistributedLockDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 问题025：Redis 分布式锁最小正确集示例实现。
 *
 * 本实现完整覆盖面试关键点：
 * 1. 加锁：SET key value NX PX ttl（原子占锁 + 过期时间防死锁）。
 * 2. 解锁：Lua 思路“比对 value 后删除”，避免误删他人锁。
 * 3. watchdog：业务执行超过 ttl 时自动续期，降低锁提前过期风险。
 * 4. 可靠性边界：锁只保证互斥，不保证业务绝对可靠，仍需幂等设计。
 *
 * 说明：
 * - 这里用内存结构模拟 Redis 锁键和值，便于在本工程最小化运行。
 * - 方法命名和行为保持与 Redis 语义一致，方便迁移真实 Redis 客户端实现。
 */
@Service
public class RedisDistributedLockDemoServiceImpl implements RedisDistributedLockDemoService {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLockDemoServiceImpl.class);

    /** 模拟 Redis 锁空间。key=锁名，value=锁实体。 */
    private final Map<String, LockEntry> lockStore = new ConcurrentHashMap<String, LockEntry>();
    /** 每个锁对应一个 watchdog 状态，避免重复创建续期线程。 */
    private final Map<String, AtomicBoolean> watchdogRunning = new ConcurrentHashMap<String, AtomicBoolean>();

    @Override
    public RedisDistributedLockResponse run(RedisDistributedLockRequest request) {
        long now = Instant.now().toEpochMilli();
        String lockKey = request == null ? null : request.getLockKey();
        String clientId = request == null ? null : request.getClientId();
        long ttl = request == null ? 0 : request.getLockTtlMillis();
        long workMillis = request == null ? 0 : request.getBizWorkMillis();
        boolean enableWatchdog = request != null && request.isEnableWatchdog();

        if (!isValid(lockKey, clientId, ttl, workMillis)) {
            return build(false, "PARAM_VALIDATION", "参数非法：lockKey/clientId不能为空，ttl和workMillis需>0",
                    null, false, false);
        }

        // 唯一 token：用作锁归属标识。解锁时必须比对该 token，防止误删他人锁。
        String ownerToken = clientId + "-" + UUID.randomUUID().toString();

        // 等价于：SET key value NX PX ttl
        boolean locked = setNxPx(lockKey, ownerToken, ttl, now);
        if (!locked) {
            return build(false, "LOCK_NOT_ACQUIRED", "加锁失败：已有持有者，未进入业务区", ownerToken,
                    false, false);
        }

        boolean renewed = false;
        boolean unlockedByLua = false;
        String stage = "BUSINESS_DONE";
        String message = "业务执行完成，准备安全解锁";
        boolean success = true;
        try {
            // 业务执行较长时，开启 watchdog 周期续期，避免 ttl 到期后锁被他人抢走。
            if (enableWatchdog && workMillis > ttl) {
                renewed = startWatchdog(lockKey, ownerToken, ttl);
            }

            simulateBusinessWork(workMillis);
        } finally {
            // 等价 Lua：if get(key)==value then del(key) else return 0 end
            unlockedByLua = unlockByLuaCompareAndDelete(lockKey, ownerToken);
            log.info("q025 unlock result, lockKey={}, ownerToken={}, unlocked={}", lockKey, ownerToken, unlockedByLua);
        }
        if (!unlockedByLua) {
            stage = "UNLOCK_NOT_OWNER";
            message = "业务已结束，但未通过Lua删除锁（可能锁已过期或归属变更）";
        }
        return build(success, stage, message, ownerToken, renewed, unlockedByLua);
    }

    /**
     * SET NX PX 语义模拟。
     *
     * 关键点：
     * 1. NX：仅当 key 不存在（或已过期）时可设置成功。
     * 2. PX：设置毫秒级过期时间，防止持锁进程宕机导致死锁。
     */
    private boolean setNxPx(String lockKey, String ownerToken, long ttlMillis, long now) {
        synchronized (getMonitor(lockKey)) {
            LockEntry entry = lockStore.get(lockKey);
            if (entry != null && entry.expireAt > now) {
                return false;
            }
            lockStore.put(lockKey, new LockEntry(ownerToken, now + ttlMillis));
            return true;
        }
    }

    /**
     * Lua 安全解锁语义模拟。
     *
     * 为什么不能直接 DEL：
     * - 若当前线程执行慢，锁已过期并被其他线程重新获得，直接 DEL 会误删“别人的锁”。
     * - 先比对 value（ownerToken）再删除，可保证“只删除自己持有的锁”。
     */
    private boolean unlockByLuaCompareAndDelete(String lockKey, String ownerToken) {
        synchronized (getMonitor(lockKey)) {
            LockEntry entry = lockStore.get(lockKey);
            if (entry == null) {
                return false;
            }
            if (!ownerToken.equals(entry.ownerToken)) {
                return false;
            }
            lockStore.remove(lockKey);
            AtomicBoolean running = watchdogRunning.get(lockKey);
            if (running != null) {
                running.set(false);
            }
            return true;
        }
    }

    /**
     * watchdog 续期机制。
     *
     * 机制说明：
     * 1. 周期检查锁归属仍是自己时，刷新 expireAt。
     * 2. 一旦锁归属变化/锁消失，立即停止续期。
     * 3. 续期周期通常小于 ttl（此处使用 ttl/3），避免临近过期才续期造成抖动。
     */
    private boolean startWatchdog(String lockKey, String ownerToken, long ttlMillis) {
        AtomicBoolean running = watchdogRunning.computeIfAbsent(lockKey, k -> new AtomicBoolean(false));
        if (!running.compareAndSet(false, true)) {
            return true;
        }

        long interval = Math.max(50L, ttlMillis / 3);
        Thread t = new Thread(() -> {
            while (running.get()) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    running.set(false);
                    break;
                }
                synchronized (getMonitor(lockKey)) {
                    LockEntry entry = lockStore.get(lockKey);
                    long now = Instant.now().toEpochMilli();
                    if (entry == null || !ownerToken.equals(entry.ownerToken) || entry.expireAt <= now) {
                        running.set(false);
                        break;
                    }
                    entry.expireAt = now + ttlMillis;
                }
            }
        }, "q025-watchdog-" + lockKey);
        t.setDaemon(true);
        t.start();
        return true;
    }

    private Object getMonitor(String lockKey) {
        return lockKey.intern();
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
        response.setMessage(message);
        response.setLockOwnerToken(ownerToken);
        response.setWatchdogRenewed(renewed);
        response.setUnlockedByLua(unlocked);
        // 幂等提示固定给出，强调“锁只保互斥，业务仍需幂等”。
        response.setIdempotentHint(true);
        return response;
    }

    /**
     * 锁实体。
     * ownerToken：锁归属唯一标识。
     * expireAt：过期时间戳（毫秒）。
     */
    private static class LockEntry {
        private final String ownerToken;
        private volatile long expireAt;

        private LockEntry(String ownerToken, long expireAt) {
            this.ownerToken = ownerToken;
            this.expireAt = expireAt;
        }
    }
}
