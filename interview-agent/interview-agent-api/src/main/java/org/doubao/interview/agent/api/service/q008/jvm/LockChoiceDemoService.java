package org.doubao.interview.agent.api.service.q008.jvm;

import org.doubao.interview.agent.api.dto.q008.jvm.LockChoiceDemoRequest;
import org.doubao.interview.agent.api.dto.q008.jvm.LockChoiceDemoResponse;

/** 问题008(JVM)：synchronized 与 ReentrantLock 选择服务。 */
public interface LockChoiceDemoService {
    LockChoiceDemoResponse run(LockChoiceDemoRequest request);
}
