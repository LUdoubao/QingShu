package org.doubao.interview.agent.server.service.impl.redis.q017;

import org.doubao.interview.agent.api.dto.redis.q017.AofRewriteRequest;
import org.doubao.interview.agent.api.dto.redis.q017.AofRewriteResponse;
import org.doubao.interview.agent.api.service.redis.q017.AofRewriteDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 问题017：AOF rewrite 示例实现。
 *
 * 核心概念解释：
 * 1. AOF 原始日志会记录每次写命令，长期运行后文件会不断膨胀。
 * 2. rewrite 的目标是“保留最终状态所需的最小命令集”，例如把多次 INCR 压缩为一次 SET。
 * 3. 真正的 Redis 中 rewrite 由子进程执行，主进程持续处理写请求；
 *    rewrite 期间的新写会进入增量缓冲，最后再重放到新 AOF 文件。
 * 4. 这样可以减少磁盘占用，并加快重启恢复（重放命令更少）。
 */
@Service
public class AofRewriteDemoServiceImpl implements AofRewriteDemoService {

    private static final Logger log = LoggerFactory.getLogger(AofRewriteDemoServiceImpl.class);

    @Override
    public AofRewriteResponse rewrite(AofRewriteRequest request) {
        int incrTimes = request == null ? 0 : request.getIncrTimes();
        boolean incremental = request != null && request.isSimulateIncrementalWrites();

        if (incrTimes <= 0) {
            AofRewriteResponse fail = new AofRewriteResponse();
            fail.setSuccess(false);
            fail.setStage("PARAM_VALIDATION");
            fail.setMessage("incrTimes 必须大于 0");
            return fail;
        }

        // 原始 AOF：记录所有历史写命令。
        List<String> originalAof = new ArrayList<String>();
        int value = 0;
        for (int i = 0; i < incrTimes; i++) {
            originalAof.add("INCR counter");
            value++;
        }

        // 模拟 rewrite 开始时的“内存快照值”。
        int snapshotValue = value;

        // 模拟 rewrite 期间主线程仍有新写入，这些命令会先进入增量缓冲区。
        List<String> incrementalBuffer = new ArrayList<String>();
        if (incremental) {
            for (int i = 0; i < 5; i++) {
                incrementalBuffer.add("INCR counter");
                value++;
            }
        }

        // 子进程 rewrite：基于快照值生成最小恢复命令集。
        List<String> rewrittenAof = new ArrayList<String>();
        rewrittenAof.add("SET counter " + snapshotValue);

        // 子进程完成后，主进程把 rewrite 期间的增量命令重放到新AOF末尾。
        rewrittenAof.addAll(incrementalBuffer);

        AofRewriteResponse response = new AofRewriteResponse();
        response.setSuccess(true);
        response.setStage("AOF_REWRITE_DONE");
        response.setBeforeCommandCount(originalAof.size() + incrementalBuffer.size());
        response.setAfterCommandCount(rewrittenAof.size());
        response.setFinalValue(value);
        response.setSampleBefore(sampleOriginal(originalAof, incrementalBuffer));
        response.setSampleAfter(Collections.unmodifiableList(rewrittenAof));
        response.setMessage("AOF rewrite 完成：历史命令已压缩为最小命令集，并合并增量重放。"
                + "命令数从 " + response.getBeforeCommandCount() + " 降为 " + response.getAfterCommandCount()
                + "，可减少文件膨胀并提升恢复效率。");

        log.info("q017 rewrite done, incrTimes={}, incrementalWrites={}, beforeCount={}, afterCount={}, finalValue={}",
                incrTimes, incremental, response.getBeforeCommandCount(), response.getAfterCommandCount(), value);
        return response;
    }

    /**
     * 返回原始日志样本，便于面试演示“重写前日志冗余”。
     */
    private List<String> sampleOriginal(List<String> originalAof, List<String> incrementalBuffer) {
        List<String> sample = new ArrayList<String>();
        int limit = Math.min(6, originalAof.size());
        for (int i = 0; i < limit; i++) {
            sample.add(originalAof.get(i));
        }
        if (originalAof.size() > limit) {
            sample.add("... x" + (originalAof.size() - limit));
        }
        if (!incrementalBuffer.isEmpty()) {
            sample.add("[rewrite期间增量写入] " + incrementalBuffer.size() + " 条 INCR");
        }
        return sample;
    }
}
