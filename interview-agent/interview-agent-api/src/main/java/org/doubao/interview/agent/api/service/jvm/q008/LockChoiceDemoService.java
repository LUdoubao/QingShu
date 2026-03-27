package org.doubao.interview.agent.api.service.jvm.q008;

import org.doubao.interview.agent.api.dto.jvm.q008.LockChoiceDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q008.LockChoiceDemoResponse;

/** 问题008(JVM)：synchronized 与 ReentrantLock 选择服务。 */
public interface LockChoiceDemoService {
    LockChoiceDemoResponse run(LockChoiceDemoRequest request);
}
