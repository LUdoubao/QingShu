package org.doubao.interview.agent.api.service.redis.q009;

import org.doubao.interview.agent.api.dto.redis.q009.CacheBreakdownQueryRequest;
import org.doubao.interview.agent.api.dto.redis.q009.CacheBreakdownQueryResponse;

/**
 * 问题009：缓存击穿治理服务接口。
 *
 * 该接口抽象了“热点 key 查询 + 击穿治理”的统一入口，
 * 方便后续替换不同实现（内存版、Redis版、分布式锁版等）。
 */
public interface CacheBreakdownGovernanceService {

    /**
     * 查询热点 key，并返回治理结果。
     *
     * 典型执行路径：参数校验 -> 限流 -> 本地缓存 -> 远端缓存 ->
     * 互斥重建 / 逻辑过期异步刷新 / 不过期主动刷新。
     *
     * @param request 查询请求
     * @return 查询响应
     */
    CacheBreakdownQueryResponse query(CacheBreakdownQueryRequest request);
}