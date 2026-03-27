package org.doubao.interview.agent.api.dto.q007.jvm;

import java.io.Serializable;

/** 问题007(JVM)：synchronized 演示请求。 */
public class SynchronizedDemoRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private int threadCount;
    private int incrementPerThread;

    public int getThreadCount() { return threadCount; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
    public int getIncrementPerThread() { return incrementPerThread; }
    public void setIncrementPerThread(int incrementPerThread) { this.incrementPerThread = incrementPerThread; }
}
