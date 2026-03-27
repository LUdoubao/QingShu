package org.doubao.interview.agent.server.service.impl.redis.q016;

import org.doubao.interview.agent.api.dto.redis.q016.PersistenceProfile;
import org.doubao.interview.agent.api.dto.redis.q016.RedisPersistenceCompareRequest;
import org.doubao.interview.agent.api.dto.redis.q016.RedisPersistenceCompareResponse;
import org.doubao.interview.agent.api.service.redis.q016.RedisPersistenceCompareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 问题016：RDB 与 AOF 区别示例实现。
 *
 * 设计目标：
 * 1. 将面试里的抽象结论转成可运行、可观测的结构化结果。
 * 2. 统一输出三个关键维度：
 *    - 恢复速度（recoverCostMs）
 *    - 数据安全（dataLossWindowSeconds）
 *    - 文件体积（fileSizeKb）
 * 3. 给出“同时开启 RDB + AOF”时的组合权衡，帮助形成工程化回答。
 *
 * 说明：
 * 该实现是教学模拟，不依赖真实 Redis 文件，但计算逻辑遵循常见经验规律。
 */
@Service
public class RedisPersistenceCompareServiceImpl implements RedisPersistenceCompareService {

    private static final Logger log = LoggerFactory.getLogger(RedisPersistenceCompareServiceImpl.class);

    @Override
    public RedisPersistenceCompareResponse compare(RedisPersistenceCompareRequest request) {
        long writeOps = request == null ? 0 : request.getWriteOps();
        int snapshotSeconds = request == null ? 0 : request.getRdbSnapshotSeconds();
        String fsyncPolicy = request == null ? null : request.getAofFsyncPolicy();

        if (writeOps <= 0 || snapshotSeconds <= 0 || !isValidFsyncPolicy(fsyncPolicy)) {
            RedisPersistenceCompareResponse fail = new RedisPersistenceCompareResponse();
            fail.setSuccess(false);
            fail.setStage("PARAM_VALIDATION");
            fail.setSummary("参数非法：writeOps>0、rdbSnapshotSeconds>0、aofFsyncPolicy需为always/everysec/no");
            return fail;
        }

        PersistenceProfile rdb = buildRdbProfile(writeOps, snapshotSeconds);
        PersistenceProfile aof = buildAofProfile(writeOps, fsyncPolicy);
        PersistenceProfile hybrid = buildHybridProfile(rdb, aof);

        RedisPersistenceCompareResponse response = new RedisPersistenceCompareResponse();
        response.setSuccess(true);
        response.setStage("COMPARE_DONE");
        response.setRdbProfile(rdb);
        response.setAofProfile(aof);
        response.setHybridProfile(hybrid);
        response.setSummary(buildSummary(rdb, aof, hybrid));

        log.info("q016 compare done, writeOps={}, snapshotSeconds={}, fsyncPolicy={}, rdbRecoverMs={}, aofRecoverMs={}, hybridRecoverMs={}",
                writeOps, snapshotSeconds, fsyncPolicy,
                rdb.getRecoverCostMs(), aof.getRecoverCostMs(), hybrid.getRecoverCostMs());
        return response;
    }

    /**
     * RDB 画像计算。
     *
     * 理解要点：
     * 1. RDB 是时间点快照，不记录每条写命令。
     * 2. 文件体积通常更小，恢复时直接加载快照，速度通常更快。
     * 3. 数据丢失窗口约等于“上次快照到故障发生”的区间上界，通常与快照间隔同量级。
     */
    private PersistenceProfile buildRdbProfile(long writeOps, int snapshotSeconds) {
        PersistenceProfile p = new PersistenceProfile();
        p.setMode("RDB");

        long fileSizeKb = Math.max(64, writeOps / 3);
        long recoverCostMs = Math.max(80, fileSizeKb / 2);
        double dataLossWindow = snapshotSeconds;

        p.setFileSizeKb(fileSizeKb);
        p.setRecoverCostMs(recoverCostMs);
        p.setDataLossWindowSeconds(dataLossWindow);
        p.setExplanation("RDB按时间点生成快照，文件更紧凑、恢复通常更快，但可能丢失最近一段未快照数据。"
                + "当前估算中，丢失窗口约为快照间隔上界=" + snapshotSeconds + "秒。");
        return p;
    }

    /**
     * AOF 画像计算。
     * <p>
     * 理解要点：
     * 1. AOF 追加写命令，数据完整性通常更好。
     * 2. 恢复需要重放日志，写入越多通常恢复越慢。
     * 3. fsync策略决定“可接受的数据丢失窗口”：
     *    - always：每条命令都刷盘，窗口最小（近似0）
     *    - everysec：每秒刷盘，窗口通常约1秒
     *    - no：交给OS调度，窗口不确定且更大
     */
    private PersistenceProfile buildAofProfile(long writeOps, String fsyncPolicy) {
        PersistenceProfile p = new PersistenceProfile();
        p.setMode("AOF(" + fsyncPolicy + ")");

        long fileSizeKb = Math.max(96, writeOps);
        long recoverCostMs = Math.max(120, writeOps / 2);
        double dataLossWindow = estimateAofLossWindow(fsyncPolicy);

        p.setFileSizeKb(fileSizeKb);
        p.setRecoverCostMs(recoverCostMs);
        p.setDataLossWindowSeconds(dataLossWindow);
        p.setExplanation("AOF记录写命令追加日志，数据更完整；但恢复时需重放命令，通常比RDB慢。"
                + "fsync=" + fsyncPolicy + " 时，估算丢失窗口约为 " + dataLossWindow + " 秒。");
        return p;
    }

    /**
     * 混合模式画像。
     * <p>
     * 面试表达建议：
     * 1. 用AOF保障更高数据完整性。
     * 2. 用RDB做冷备和辅助快速恢复。
     * 3. 成本是运维复杂度更高、总磁盘占用更大。
     */
    private PersistenceProfile buildHybridProfile(PersistenceProfile rdb, PersistenceProfile aof) {
        PersistenceProfile p = new PersistenceProfile();
        p.setMode("RDB+AOF");

        long recoverCostMs = Math.min(rdb.getRecoverCostMs() + 30, aof.getRecoverCostMs());
        long fileSizeKb = rdb.getFileSizeKb() + aof.getFileSizeKb();
        double dataLossWindow = Math.min(rdb.getDataLossWindowSeconds(), aof.getDataLossWindowSeconds());

        p.setRecoverCostMs(recoverCostMs);
        p.setFileSizeKb(fileSizeKb);
        p.setDataLossWindowSeconds(dataLossWindow);
        p.setExplanation("同时开启时，通常优先使用AOF保证数据完整性，RDB作为备份与恢复加速辅助。"
                + "综合表现是：数据安全更好、恢复可优化，但磁盘成本更高。");
        return p;
    }

    private double estimateAofLossWindow(String fsyncPolicy) {
        if ("always".equalsIgnoreCase(fsyncPolicy)) {
            return 0.0D;
        }
        if ("everysec".equalsIgnoreCase(fsyncPolicy)) {
            return 1.0D;
        }
        return 5.0D;
    }

    private boolean isValidFsyncPolicy(String fsyncPolicy) {
        if (fsyncPolicy == null) {
            return false;
        }
        String p = fsyncPolicy.toLowerCase();
        return "always".equals(p) || "everysec".equals(p) || "no".equals(p);
    }

    private String buildSummary(PersistenceProfile rdb, PersistenceProfile aof, PersistenceProfile hybrid) {
        return "权衡结论：RDB恢复更快(" + rdb.getRecoverCostMs() + "ms)且文件更小(" + rdb.getFileSizeKb() + "KB)，"
                + "AOF数据丢失窗口更小(" + aof.getDataLossWindowSeconds() + "s)但恢复通常更慢；"
                + "混合模式在数据安全与恢复能力之间取得工程平衡。";
    }
}
