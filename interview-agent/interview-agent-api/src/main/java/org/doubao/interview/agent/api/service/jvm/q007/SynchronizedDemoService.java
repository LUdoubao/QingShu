package org.doubao.interview.agent.api.service.jvm.q007;

import org.doubao.interview.agent.api.dto.jvm.q007.SynchronizedDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q007.SynchronizedDemoResponse;

/** 问题007(JVM)：synchronized 演示服务。 */
public interface SynchronizedDemoService {
    SynchronizedDemoResponse run(SynchronizedDemoRequest request);
}
