package org.doubao.interview.agent.api.dto.redis.q017;

import java.io.Serializable;

/** 问题017：AOF rewrite 演示请求。 */
public class AofRewriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private int incrTimes;
    private boolean simulateIncrementalWrites;

    public int getIncrTimes() { return incrTimes; }
    public void setIncrTimes(int incrTimes) { this.incrTimes = incrTimes; }
    public boolean isSimulateIncrementalWrites() { return simulateIncrementalWrites; }
    public void setSimulateIncrementalWrites(boolean simulateIncrementalWrites) { this.simulateIncrementalWrites = simulateIncrementalWrites; }
}
