package org.doubao.interview.agent.api.service.jvm.q009;

import org.doubao.interview.agent.api.dto.jvm.q009.ReentrantLockDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q009.ReentrantLockDemoResponse;

/** 问题009(JVM)：可重入锁演示服务。 */
public interface ReentrantLockDemoService {
    ReentrantLockDemoResponse run(ReentrantLockDemoRequest request);
}
