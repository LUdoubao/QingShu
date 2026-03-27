package org.doubao.interview.agent.api.service.q007.jvm;

import org.doubao.interview.agent.api.dto.q007.jvm.SynchronizedDemoRequest;
import org.doubao.interview.agent.api.dto.q007.jvm.SynchronizedDemoResponse;

/** 问题007(JVM)：synchronized 演示服务。 */
public interface SynchronizedDemoService {
    SynchronizedDemoResponse run(SynchronizedDemoRequest request);
}
