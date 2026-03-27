package org.doubao.interview.agent.api.service.jvm.q011;

import org.doubao.interview.agent.api.dto.jvm.q011.PessimisticOptimisticLockRequest;
import org.doubao.interview.agent.api.dto.jvm.q011.PessimisticOptimisticLockResponse;

/** Q011(JVM): pessimistic vs optimistic lock demo service. */
public interface PessimisticOptimisticLockService {
    PessimisticOptimisticLockResponse run(PessimisticOptimisticLockRequest request);
}
