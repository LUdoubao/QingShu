package org.doubao.interview.agent.api.dto.jvm.q011;

import java.io.Serializable;

/** Q011(JVM): pessimistic vs optimistic lock request. */
public class PessimisticOptimisticLockRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String lockType;
    private int threads;
    private int incrementsPerThread;

    public String getLockType() { return lockType; }
    public void setLockType(String lockType) { this.lockType = lockType; }
    public int getThreads() { return threads; }
    public void setThreads(int threads) { this.threads = threads; }
    public int getIncrementsPerThread() { return incrementsPerThread; }
    public void setIncrementsPerThread(int incrementsPerThread) { this.incrementsPerThread = incrementsPerThread; }
}
