package org.doubao.interview.agent.api.dto.q016;

import java.io.Serializable;

/**
 * 问题016：RDB/AOF 对比演示请求。
 *
 * 该请求用于模拟一段写入流量在不同持久化策略下的表现，
 * 重点观察三个维度：
 * 1. 恢复速度
 * 2. 数据丢失窗口
 * 3. 文件体积
 */
public class RedisPersistenceCompareRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 写入命令条数，用于模拟业务写入规模。
     */
    private int writeOps;

    /**
     * RDB 快照间隔（秒），用于计算潜在数据丢失窗口。
     */
    private int rdbSnapshotSeconds;

    /**
     * AOF 刷盘策略：always/everysec/no。
     */
    private String aofFsyncPolicy;

    public int getWriteOps() {
        return writeOps;
    }

    public void setWriteOps(int writeOps) {
        this.writeOps = writeOps;
    }

    public int getRdbSnapshotSeconds() {
        return rdbSnapshotSeconds;
    }

    public void setRdbSnapshotSeconds(int rdbSnapshotSeconds) {
        this.rdbSnapshotSeconds = rdbSnapshotSeconds;
    }

    public String getAofFsyncPolicy() {
        return aofFsyncPolicy;
    }

    public void setAofFsyncPolicy(String aofFsyncPolicy) {
        this.aofFsyncPolicy = aofFsyncPolicy;
    }
}
