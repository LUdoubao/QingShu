package org.doubao.interview.agent.api.service.redis.q025;

import org.doubao.interview.agent.api.dto.redis.q025.RedisDistributedLockRequest;
import org.doubao.interview.agent.api.dto.redis.q025.RedisDistributedLockResponse;

/** 问题025：Redis 分布式锁演示服务。 */
public interface RedisDistributedLockDemoService {
    RedisDistributedLockResponse run(RedisDistributedLockRequest request);
}
