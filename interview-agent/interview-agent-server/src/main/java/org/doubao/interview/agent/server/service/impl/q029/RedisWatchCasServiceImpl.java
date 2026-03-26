package org.doubao.interview.agent.server.service.impl.q029;

import org.doubao.interview.agent.api.dto.q029.RedisWatchCasRequest;
import org.doubao.interview.agent.api.dto.q029.RedisWatchCasResponse;
import org.doubao.interview.agent.api.service.q029.RedisWatchCasService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 问题 029:WATCH 机制实现 Redis 乐观锁示例。
 *
 * 【核心原理】
 * 基于 Redis 的 WATCH/MULTI/EXEC 机制实现乐观锁（Optimistic Locking），采用 CAS
 * （Compare-And-Swap）思想：先读取当前值并计算新值，然后在事务中提交更新。
 *
 * 【典型流程】
 * 1. WATCH key：监听目标键，记录版本号（Redis 内部通过数据版本判断是否被修改）
 * 2. GET 当前值：读取旧值并在客户端计算新值（例如：oldValue + delta）
 * 3. MULTI：开启事务，将写命令加入队列
 * 4. SET 新值：将计算好的新值写入
 * 5. EXEC 提交：
 *    - 成功：WATCH 期间 key 未被修改，事务执行，返回结果
 *    - 失败：WATCH 检测到 key 被其他客户端修改，EXEC 返回 null/空列表，事务回滚
 * 6. 重试机制：失败后按 maxRetry 参数进行重试
 *
 * 【WATCH 工作机制】
 * - WATCH 会监控指定 key 的版本号
 * - 如果在 WATCH 之后、EXEC 之前，有其他客户端修改了该 key
 * - EXEC 执行时会发现版本号不匹配，返回 nil（Java 中为 null），事务不执行
 * - 如果 EXEC 成功，WATCH 自动解除；如果失败或手动 UNWATCH，需要显式解除监控
 *
 * 【适用场景】
 * - 低冲突场景：多个客户端并发访问但实际修改概率低
 *   例如：商品库存扣减（大部分时间只有一个人在买）、计数器递增
 * - 优势：实现简单，无需复杂的锁机制
 *
 * 【不适用场景】
 * - 高冲突场景：大量客户端同时修改同一 key
 *   例如：秒杀活动的热门商品、抢红包
 * - 问题：会导致大量重试，浪费资源
 * - 替代方案：使用 Lua 脚本（原子性更好）或分布式锁
 */
@Service
public class RedisWatchCasServiceImpl implements RedisWatchCasService {

    private static final Logger log = LoggerFactory.getLogger(RedisWatchCasServiceImpl.class);
    private static final String PREFIX = "q029:watch:";

    private final StringRedisTemplate redisTemplate;

    public RedisWatchCasServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 基于 WATCH 机制实现 Redis 乐观锁自增操作。
     * <p>
     * 【执行流程】
     * 1. 参数校验：检查 key、delta（增量）、maxRetry（最大重试次数）是否合法
     * 2. 进入重试循环：最多尝试 maxRetry 次
     * 3. 每次重试执行以下步骤：
     *    a. WATCH key：开始监控 key 的变化
     *    b. GET 当前值：读取 oldValue 并计算 newValue = oldValue + delta
     *    c. MULTI：开启 Redis 事务
     *    d. SET 新值：将 newValue 写入
     *    e. EXEC：提交事务
     *       - 成功：EXEC 返回非空列表，表示更新成功
     *       - 失败：EXEC 返回 null/空，表示检测到并发修改
     * 4. 根据 EXEC 结果决定：
     *    - 成功：立即返回成功响应
     *    - 失败：继续下一次重试
     * 5. 重试次数用尽：返回失败响应，建议改用 Lua 或其他方案
     *
     * @param request 请求参数（包含 key、delta、maxRetry）
     * @return 响应结果（包含成功状态、阶段标识、旧值/新值、重试次数等）
     */
    public RedisWatchCasResponse incrementByWatch(RedisWatchCasRequest request) {
        // ========== Step 1: 解析请求参数 ==========
        String key = request == null ? null : request.getKey();
        int delta = request == null ? 0 : request.getDelta();
        int maxRetry = request == null ? 0 : request.getMaxRetry();
        
        // ========== Step 2: 参数校验 ==========
        if (!valid(key, delta, maxRetry)) {
            return build(false, "PARAM_VALIDATION", "参数非法：key 不能为空，delta/maxRetry 需>0", key, null, null, 0);
        }
        
        // ========== Step 3: 构造 Redis Key ==========
        final String redisKey = PREFIX + key;
                
        // ========== Step 4: 重试循环 ==========
        // 最多尝试 maxRetry 次，直到成功或次数用尽
        for (int i = 1; i <= maxRetry; i++) {
            final int retryNo = i;  // 当前重试次数（用于日志追踪）
            try {
                // 使用 Spring Data Redis 执行 RedisConnection 回调
                RedisWatchCasResponse response = redisTemplate.execute((RedisConnection connection) -> {
                    // ========== Step 4.1: WATCH 监控 key ==========
                    // 开始监控目标 key，如果在 WATCH 之后、EXEC 之前 key 被其他客户端修改
                    // EXEC 会返回 null，表示事务提交失败
                    byte[] k = redisKey.getBytes(StandardCharsets.UTF_8);
                    connection.watch(k);
                    
                    // ========== Step 4.2: 读取旧值并计算新值 ==========
                    // 这是 CAS（Compare-And-Swap）思想的体现：
                    // 1. Compare：读取当前值作为比较基准
                    // 2. Swap：基于旧值计算新值
                    byte[] currentBytes = connection.get(k);
                    int oldValue = currentBytes == null ? 0 : Integer.parseInt(new String(currentBytes, StandardCharsets.UTF_8));
                    int newValue = oldValue + delta;
                    
                    // ========== Step 4.3: 开启事务 ==========
                    // MULTI 之后的命令会被放入队列，直到 EXEC 才真正执行
                    connection.multi();
                                        
                    // ========== Step 4.4: 写入新值 ==========
                    // 将计算好的 newValue 加入事务队列
                    connection.set(k, String.valueOf(newValue).getBytes(StandardCharsets.UTF_8));
                                        
                    // ========== Step 4.5: 提交事务 ==========
                    // EXEC 会执行队列中的所有命令，但在此之前会检查 WATCH 的 key 是否被修改
                    List<Object> exec = connection.exec();
                    
                    // ========== Step 4.6: 判断执行结果 ==========
                    // EXEC 为 null 或空：表示 WATCH 检测到并发修改，事务提交失败
                    // EXEC 为非空列表：表示所有命令执行成功
                    if (exec == null || exec.isEmpty()) {
                        // 情况 1：事务失败（并发冲突）
                        // 需要手动解除 WATCH 监控，准备下一次重试
                        connection.unwatch();
                        return build(false, "CAS_RETRY", "检测到并发修改，EXEC 失败，触发重试", key, oldValue, null, retryNo);
                    }
                                        
                    // 情况 2：事务成功
                    // WATCH 自动解除，无需手动 unwatch
                    return build(true, "CAS_SUCCESS", "WATCH 乐观锁提交成功", key, oldValue, newValue, retryNo - 1);
                });
        
                // ========== Step 5: 检查重试结果 ==========
                // 如果响应成功，直接返回；否则继续下一次重试
                if (response != null && response.isSuccess()) {
                    return response;
                }
            } catch (DataAccessException ex) {
                // ========== Step 6: 异常处理 ==========
                // Redis 连接异常、网络故障等情况
                // 记录警告日志，继续下一次重试
                log.warn("q029 watch cas failed, key={}, retry={}, reason={}", key, i, ex.getMessage());
            }
        }
        
        // ========== Step 7: 重试次数用尽 ==========
        // 循环结束仍未成功，说明冲突过于频繁
        // 返回失败响应，并建议用户改用 Lua 脚本或其他方案
        return build(false, "CAS_EXHAUSTED", "重试次数耗尽，建议高冲突场景改用 Lua 或其他方案", key, null, null, maxRetry);
    }

    private boolean valid(String key, int delta, int maxRetry) {
        return key != null && !key.trim().isEmpty() && delta > 0 && maxRetry > 0;
    }

    private RedisWatchCasResponse build(boolean success, String stage, String message,
                                        String key, Integer oldValue, Integer newValue, int retries) {
        RedisWatchCasResponse r = new RedisWatchCasResponse();
        r.setSuccess(success);
        r.setStage(stage);
        r.setMessage(message);
        r.setKey(key);
        r.setOldValue(oldValue);
        r.setNewValue(newValue);
        r.setRetries(retries);
        return r;
    }
}
