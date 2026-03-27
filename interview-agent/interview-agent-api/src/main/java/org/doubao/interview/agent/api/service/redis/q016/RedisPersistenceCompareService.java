package org.doubao.interview.agent.api.service.redis.q016;

import org.doubao.interview.agent.api.dto.redis.q016.RedisPersistenceCompareRequest;
import org.doubao.interview.agent.api.dto.redis.q016.RedisPersistenceCompareResponse;

/**
 * 问题016：RDB 与 AOF 差异对比服务。
 */
public interface RedisPersistenceCompareService {

    /**
     * 执行 RDB/AOF/混合模式对比。
     */
    RedisPersistenceCompareResponse compare(RedisPersistenceCompareRequest request);
}
