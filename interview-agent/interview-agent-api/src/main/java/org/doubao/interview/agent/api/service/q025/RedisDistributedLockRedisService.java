package org.doubao.interview.agent.api.service.q025;

import org.doubao.interview.agent.api.dto.q025.RedisDistributedLockRequest;
import org.doubao.interview.agent.api.dto.q025.RedisDistributedLockResponse;

/**
 * 问题025：Redis 真实版本分布式锁服务。
 */
public interface RedisDistributedLockRedisService {

    /**
     * 使用 Redis 命令语义执行分布式锁流程。
     */
    RedisDistributedLockResponse runRedis(RedisDistributedLockRequest request);
}
