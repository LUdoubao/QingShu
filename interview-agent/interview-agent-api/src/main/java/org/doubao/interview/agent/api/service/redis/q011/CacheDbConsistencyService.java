package org.doubao.interview.agent.api.service.redis.q011;

import org.doubao.interview.agent.api.dto.redis.q011.CacheDbConsistencyQueryRequest;
import org.doubao.interview.agent.api.dto.redis.q011.CacheDbConsistencyResponse;
import org.doubao.interview.agent.api.dto.redis.q011.CacheDbConsistencyUpdateRequest;

/**
 * 问题011：缓存与数据库双写一致性服务接口。
 */
public interface CacheDbConsistencyService {

    /**
     * 执行“写库后删缓存 + 延迟双删 + 重试补偿 + MQ失效通知”示例。
     */
    CacheDbConsistencyResponse update(CacheDbConsistencyUpdateRequest request);

    /**
     * 查询数据，观察缓存与数据库状态。
     */
    CacheDbConsistencyResponse query(CacheDbConsistencyQueryRequest request);
}
