package org.doubao.interview.agent.api.service.redis.q029;

import org.doubao.interview.agent.api.dto.redis.q029.RedisWatchCasRequest;
import org.doubao.interview.agent.api.dto.redis.q029.RedisWatchCasResponse;

/** 问题029：WATCH 乐观锁服务。 */
public interface RedisWatchCasService {
    RedisWatchCasResponse incrementByWatch(RedisWatchCasRequest request);
}
