package org.doubao.interview.agent.api.service.q010;

import org.doubao.interview.agent.api.dto.q010.CacheAvalancheQueryRequest;
import org.doubao.interview.agent.api.dto.q010.CacheAvalancheQueryResponse;

/**
 * 问题010：缓存雪崩治理服务接口。
 */
public interface CacheAvalancheGovernanceService {

    /**
     * 查询业务 key 并执行缓存雪崩治理。
     *
     * @param request 查询请求
     * @return 治理结果
     */
    CacheAvalancheQueryResponse query(CacheAvalancheQueryRequest request);
}
