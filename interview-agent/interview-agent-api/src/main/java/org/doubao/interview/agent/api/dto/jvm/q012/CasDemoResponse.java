package org.doubao.interview.agent.api.dto.jvm.q012;

import java.io.Serializable;
import java.util.List;

/**
 * 问题 012（JVM）：CAS 演示响应对象。
 * <p>
 * 这个响应对象刻意保留了“实验结果 + 结论说明”两类信息：
 * 1. 实验结果：让面试题不止停留在概念层，而是可以直接运行验证。
 * 2. 结论说明：把实验结果映射回面试回答中的关键知识点，方便复习和讲解。
 */
public class CasDemoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 整体执行是否成功。
     */
    private boolean success;

    /**
     * 当前阶段标识。
     * <p>
     * 例如：
     * - DONE：正常完成
     * - PARAM_VALIDATION：参数校验失败
     */
    private String stage;

    /**
     * 给调用方或面试复盘时阅读的简要说明。
     */
    private String message;

    /**
     * CAS 定义说明。
     */
    private String casDefinition;

    /**
     * CAS 计数器演示结果。
     */
    private CounterDemoResult counterDemo;

    /**
     * ABA 问题演示结果。
     */
    private AbaDemoResult abaDemo;

    /**
     * CAS 单变量原子性边界演示结果。
     */
    private CompositeCasDemoResult compositeDemo;

    /**
     * 面试可直接复述的结论列表。
     */
    private List<String> interviewPoints;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCasDefinition() {
        return casDefinition;
    }

    public void setCasDefinition(String casDefinition) {
        this.casDefinition = casDefinition;
    }

    public CounterDemoResult getCounterDemo() {
        return counterDemo;
    }

    public void setCounterDemo(CounterDemoResult counterDemo) {
        this.counterDemo = counterDemo;
    }

    public AbaDemoResult getAbaDemo() {
        return abaDemo;
    }

    public void setAbaDemo(AbaDemoResult abaDemo) {
        this.abaDemo = abaDemo;
    }

    public CompositeCasDemoResult getCompositeDemo() {
        return compositeDemo;
    }

    public void setCompositeDemo(CompositeCasDemoResult compositeDemo) {
        this.compositeDemo = compositeDemo;
    }

    public List<String> getInterviewPoints() {
        return interviewPoints;
    }

    public void setInterviewPoints(List<String> interviewPoints) {
        this.interviewPoints = interviewPoints;
    }

    /**
     * CAS 计数器演示结果。
     * <p>
     * 通过多个线程同时对同一个 AtomicInteger 做 compareAndSet 循环，
     * 演示“CAS 可以在无锁场景下保证单个共享变量的原子更新”。
     */
    public static class CounterDemoResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private int threads;
        private int incrementsPerThread;
        private int expected;
        private int actual;
        private int totalRetries;
        private int maxRetryOfSingleThread;
        private long costMs;
        private boolean enableSpinHint;
        private String strategyNote;

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

        public int getExpected() {
            return expected;
        }

        public void setExpected(int expected) {
            this.expected = expected;
        }

        public int getActual() {
            return actual;
        }

        public void setActual(int actual) {
            this.actual = actual;
        }

        public int getTotalRetries() {
            return totalRetries;
        }

        public void setTotalRetries(int totalRetries) {
            this.totalRetries = totalRetries;
        }

        public int getMaxRetryOfSingleThread() {
            return maxRetryOfSingleThread;
        }

        public void setMaxRetryOfSingleThread(int maxRetryOfSingleThread) {
            this.maxRetryOfSingleThread = maxRetryOfSingleThread;
        }

        public long getCostMs() {
            return costMs;
        }

        public void setCostMs(long costMs) {
            this.costMs = costMs;
        }

        public boolean isEnableSpinHint() {
            return enableSpinHint;
        }

        public void setEnableSpinHint(boolean enableSpinHint) {
            this.enableSpinHint = enableSpinHint;
        }

        public String getStrategyNote() {
            return strategyNote;
        }

        public void setStrategyNote(String strategyNote) {
            this.strategyNote = strategyNote;
        }
    }

    /**
     * ABA 演示结果。
     * <p>
     * plainCasSucceeded 表示“普通 CAS 只看值，不看值在中间是否被改过”，
     * 这正是 ABA 问题的核心。
     * stampedCasSucceeded 则表示加入版本号后，CAS 能识别中途变化。
     */
    public static class AbaDemoResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private boolean plainCasSucceeded;
        private boolean stampedCasSucceeded;
        private String plainFlow;
        private String stampedFlow;
        private String solution;

        public boolean isPlainCasSucceeded() {
            return plainCasSucceeded;
        }

        public void setPlainCasSucceeded(boolean plainCasSucceeded) {
            this.plainCasSucceeded = plainCasSucceeded;
        }

        public boolean isStampedCasSucceeded() {
            return stampedCasSucceeded;
        }

        public void setStampedCasSucceeded(boolean stampedCasSucceeded) {
            this.stampedCasSucceeded = stampedCasSucceeded;
        }

        public String getPlainFlow() {
            return plainFlow;
        }

        public void setPlainFlow(String plainFlow) {
            this.plainFlow = plainFlow;
        }

        public String getStampedFlow() {
            return stampedFlow;
        }

        public void setStampedFlow(String stampedFlow) {
            this.stampedFlow = stampedFlow;
        }

        public String getSolution() {
            return solution;
        }

        public void setSolution(String solution) {
            this.solution = solution;
        }
    }

    /**
     * 组合字段 CAS 演示结果。
     * <p>
     * CAS 直接作用的目标通常是一个“单一共享位置”，例如一个 int、long、引用地址。
     * 当我们需要同时维护多个字段一致性时，裸用多个 CAS 往往无法保证整体原子性；
     * 更常见的做法是把多个字段封装进一个不可变对象，再对对象引用做一次 CAS。
     */
    public static class CompositeCasDemoResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private String problemStatement;
        private StateSnapshot before;
        private StateSnapshot afterAtomicReferenceCas;
        private boolean compositeUpdateSucceeded;
        private String recommendation;

        public String getProblemStatement() {
            return problemStatement;
        }

        public void setProblemStatement(String problemStatement) {
            this.problemStatement = problemStatement;
        }

        public StateSnapshot getBefore() {
            return before;
        }

        public void setBefore(StateSnapshot before) {
            this.before = before;
        }

        public StateSnapshot getAfterAtomicReferenceCas() {
            return afterAtomicReferenceCas;
        }

        public void setAfterAtomicReferenceCas(StateSnapshot afterAtomicReferenceCas) {
            this.afterAtomicReferenceCas = afterAtomicReferenceCas;
        }

        public boolean isCompositeUpdateSucceeded() {
            return compositeUpdateSucceeded;
        }

        public void setCompositeUpdateSucceeded(boolean compositeUpdateSucceeded) {
            this.compositeUpdateSucceeded = compositeUpdateSucceeded;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }

    /**
     * 组合状态快照。
     * <p>
     * 用于表示“库存 + 版本号”这种需要整体一致性的复合状态，
     * 方便演示把多个字段打包成一个不可变对象后，再对整个引用执行 CAS。
     */
    public static class StateSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private int stock;
        private int version;

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }
    }
}
