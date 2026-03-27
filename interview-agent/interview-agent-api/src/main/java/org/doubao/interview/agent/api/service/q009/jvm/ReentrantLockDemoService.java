package org.doubao.interview.agent.api.service.q009.jvm;

import org.doubao.interview.agent.api.dto.q009.jvm.ReentrantLockDemoRequest;
import org.doubao.interview.agent.api.dto.q009.jvm.ReentrantLockDemoResponse;

/** 问题009(JVM)：可重入锁演示服务。 */
public interface ReentrantLockDemoService {
    ReentrantLockDemoResponse run(ReentrantLockDemoRequest request);
}
