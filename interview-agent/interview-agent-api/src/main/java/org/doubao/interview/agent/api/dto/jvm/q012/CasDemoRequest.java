package org.doubao.interview.agent.api.dto.jvm.q012;

import java.io.Serializable;

/**
 * 问题 012（JVM）：CAS 演示请求对象。
 * <p>
 * 这个请求对象用于驱动一组“可运行”的 CAS 演示，而不是只返回概念解释。
 * 调用方可以通过调整线程数、每个线程的递增次数与是否开启自旋让步策略，
 * 观察 CAS 在不同并发压力下的表现。
 * <p>
 * 字段说明：
 * 1. threads：并发线程数，用来制造竞争。
 * 2. incrementsPerThread：每个线程执行多少次 CAS 自增。
 * 3. enableSpinHint：CAS 失败后是否调用 Thread.onSpinWait() 给 CPU 一个“我正在自旋”的提示。
 *    这个提示不会改变正确性，只是为了更贴近真实生产中的自旋优化写法。
 */
public class CasDemoRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 并发线程数。
     * <p>
     * 值越大，多个线程同时竞争同一个共享变量的概率越高，
     * CAS 失败重试次数通常也会相应增多。
     */
    private int threads;

    /**
     * 每个线程执行的自增次数。
     * <p>
     * 理论最终值 expected = threads * incrementsPerThread。
     * 如果 CAS 实现正确，那么无论竞争多激烈，最终 actual 都应与 expected 一致。
     */
    private int incrementsPerThread;

    /**
     * 是否开启自旋提示。
     * <p>
     * CAS 失败后线程不会像阻塞锁那样挂起，而是通常继续循环重试。
     * 这就是“自旋”的来源。开启该选项后，会在重试路径中调用 Thread.onSpinWait()，
     * 向 JVM/CPU 表达当前线程处于忙等状态，帮助解释“长时间自旋会消耗 CPU”这一面试点。
     */
    private boolean enableSpinHint;

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getIncrementsPerThread() {
        return incrementsPerThread;
    }

    public void setIncrementsPerThread(int incrementsPerThread) {
        this.incrementsPerThread = incrementsPerThread;
    }

    public boolean isEnableSpinHint() {
        return enableSpinHint;
    }

    public void setEnableSpinHint(boolean enableSpinHint) {
        this.enableSpinHint = enableSpinHint;
    }
}
