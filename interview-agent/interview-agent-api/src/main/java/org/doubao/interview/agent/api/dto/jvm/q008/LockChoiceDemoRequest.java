package org.doubao.interview.agent.api.dto.jvm.q008;

import java.io.Serializable;

/** 问题008(JVM)：锁选择演示请求。 */
public class LockChoiceDemoRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mode;
    private int threadCount;
    private int incrementPerThread;
    private boolean fairLock;
    private long tryLockTimeoutMillis;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public int getThreadCount() { return threadCount; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    public int getIncrementPerThread() { return incrementPerThread; }
    public void setIncrementPerThread(int incrementPerThread) { this.incrementPerThread = incrementPerThread; }
    public boolean isFairLock() { return fairLock; }
    public void setFairLock(boolean fairLock) { this.fairLock = fairLock; }
    public long getTryLockTimeoutMillis() { return tryLockTimeoutMillis; }
    public void setTryLockTimeoutMillis(long tryLockTimeoutMillis) { this.tryLockTimeoutMillis = tryLockTimeoutMillis; }
}
