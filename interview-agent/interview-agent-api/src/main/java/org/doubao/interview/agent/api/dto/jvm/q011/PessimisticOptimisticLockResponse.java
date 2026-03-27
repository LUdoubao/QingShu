package org.doubao.interview.agent.api.dto.jvm.q011;

import java.io.Serializable;

/** Q011(JVM): pessimistic vs optimistic lock response. */
public class PessimisticOptimisticLockResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String stage;
    private String message;
    private String lockType;
    private int threads;
    private int incrementsPerThread;
    private int expected;
    private int actual;
    private int retries;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLockType() { return lockType; }
    public void setLockType(String lockType) { this.lockType = lockType; }
    public int getThreads() { return threads; }
    public void setThreads(int threads) { this.threads = threads; }
    public int getIncrementsPerThread() { return incrementsPerThread; }
    public void setIncrementsPerThread(int incrementsPerThread) { this.incrementsPerThread = incrementsPerThread; }
    public int getExpected() { return expected; }
    public void setExpected(int expected) { this.expected = expected; }
    public int getActual() { return actual; }
    public void setActual(int actual) { this.actual = actual; }
    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
}
