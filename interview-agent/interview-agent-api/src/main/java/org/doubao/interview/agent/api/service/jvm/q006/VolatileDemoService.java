package org.doubao.interview.agent.api.service.jvm.q006;

import org.doubao.interview.agent.api.dto.jvm.q006.VolatileDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q006.VolatileDemoResponse;

/**
 * 问题006(JVM)：volatile 演示服务。
 */
public interface VolatileDemoService {

    VolatileDemoResponse run(VolatileDemoRequest request);
}
