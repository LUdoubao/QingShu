package org.doubao.interview.agent.api.dto.jvm.q009;

import java.io.Serializable;

/** 问题009(JVM)：可重入锁演示请求。 */
public class ReentrantLockDemoRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mode;
    private int depth;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
}
