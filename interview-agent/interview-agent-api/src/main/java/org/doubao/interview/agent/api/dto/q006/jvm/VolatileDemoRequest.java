package org.doubao.interview.agent.api.dto.q006.jvm;

import java.io.Serializable;

/**
 * 问题006(JVM)：volatile 演示请求。
 */
public class VolatileDemoRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工作线程数量，用于演示 count++ 的并发丢失。 */
    private int threadCount;

    /** 每个线程自增次数。 */
    private int incrementPerThread;

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getIncrementPerThread() {
        return incrementPerThread;
    }

    public void setIncrementPerThread(int incrementPerThread) {
        this.incrementPerThread = incrementPerThread;
    }
}
