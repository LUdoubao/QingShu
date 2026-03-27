package org.doubao.interview.agent.api.service.jvm.q012;

import org.doubao.interview.agent.api.dto.jvm.q012.CasDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q012.CasDemoResponse;

/**
 * 问题 012（JVM）：CAS 演示服务接口。
 */
public interface CasDemoService {

    /**
     * 执行 CAS 面试题演示。
     *
     * @param request 演示请求
     * @return 演示结果
     */
    CasDemoResponse run(CasDemoRequest request);
}
