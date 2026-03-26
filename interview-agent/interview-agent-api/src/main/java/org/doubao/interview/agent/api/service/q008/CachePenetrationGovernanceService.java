package org.doubao.interview.agent.api.service.q008;

import org.doubao.interview.agent.api.dto.q008.CachePenetrationCheckRequest;
import org.doubao.interview.agent.api.dto.q008.CachePenetrationCheckResponse;

/**
 * 问题008：缓存穿透治理服务。
 *
 * 抽象一个稳定的 API 接口，便于后续替换不同实现（本地内存版 / Redis版 / 生产版）。
 */
public interface CachePenetrationGovernanceService {

    /**
     * 执行缓存穿透治理链路演示。
     *
     * 链路顺序：参数校验 -> 限流 -> 缓存命中 -> 布隆过滤器 -> 数据库查询 -> 回填缓存。
     *
     * @param request 查询请求
     * @return 治理结果（包含阶段标识与说明）
     */
    CachePenetrationCheckResponse check(CachePenetrationCheckRequest request);
}