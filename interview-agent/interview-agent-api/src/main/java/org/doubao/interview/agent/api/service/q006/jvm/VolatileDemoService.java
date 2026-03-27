package org.doubao.interview.agent.api.service.q006.jvm;

import org.doubao.interview.agent.api.dto.q006.jvm.VolatileDemoRequest;
import org.doubao.interview.agent.api.dto.q006.jvm.VolatileDemoResponse;

/**
 * 问题006(JVM)：volatile 演示服务。
 */
public interface VolatileDemoService {

    VolatileDemoResponse run(VolatileDemoRequest request);
}
