package org.doubao.interview.agent.api.service.redis.q033;

import org.doubao.interview.agent.api.dto.redis.q033.BloomFilterCheckRequest;
import org.doubao.interview.agent.api.dto.redis.q033.BloomFilterCheckResponse;

/** 问题033：Bloom Filter 防穿透服务。 */
public interface BloomFilterDemoService {
    BloomFilterCheckResponse check(BloomFilterCheckRequest request);
    BloomFilterCheckResponse rebuild();
}
