package org.doubao.search.service.index.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.search.service.entity.QuoteSearchSyncData;
import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.entity.SearchSuggestTermDO;
import org.doubao.search.service.entity.SearchTermIndexDO;
import org.doubao.search.service.index.SearchDocumentBuilder;
import org.doubao.search.service.index.SearchIndexService;
import org.doubao.search.service.index.SearchTermBuilder;
import org.doubao.search.service.index.SuggestTermBuilder;
import org.doubao.search.service.mapper.QuoteSearchSourceMapper;
import org.doubao.search.service.mapper.SearchDocIndexMapper;
import org.doubao.search.service.mapper.SearchSuggestTermMapper;
import org.doubao.search.service.mapper.SearchTermIndexMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MysqlSearchIndexService implements SearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(MysqlSearchIndexService.class);
    private static final int REBUILD_BATCH_SIZE = 500;
    private static final int REBUILD_PARALLELISM = 4;
    private static final int REBUILD_PROGRESS_LOG_BATCH_INTERVAL = 10;

    @Resource
    private QuoteSearchSourceMapper quoteSearchSourceMapper;
    @Resource
    private SearchDocumentBuilder searchDocumentBuilder;
    @Resource
    private SearchTermBuilder searchTermBuilder;
    @Resource
    private SuggestTermBuilder suggestTermBuilder;
    @Resource
    private SearchDocIndexMapper searchDocIndexMapper;
    @Resource
    private SearchTermIndexMapper searchTermIndexMapper;
    @Resource
    private SearchSuggestTermMapper searchSuggestTermMapper;
    @Resource
    private CommonTaskExecutor commonTaskExecutor;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public void upsertDocument(String bizType, Long bizId) {
        if (!"quote".equals(bizType)) {
            return;
        }
        QuoteSearchSyncData syncData = quoteSearchSourceMapper.selectQuoteSyncData(bizId);
        if (syncData == null) {
            deleteDocument(bizType, bizId);
            return;
        }
        SearchDocIndexDO doc = searchDocumentBuilder.build(syncData);
        searchDocIndexMapper.upsert(doc);
        searchTermIndexMapper.deleteByBiz(bizType, bizId);
        List<SearchTermIndexDO> terms = searchTermBuilder.build(doc);
        if (!terms.isEmpty()) {
            searchTermIndexMapper.batchInsert(terms);
        }
        searchSuggestTermMapper.deleteBySource("quote", bizId);
        List<SearchSuggestTermDO> suggestTerms = suggestTermBuilder.build(doc);
        if (!suggestTerms.isEmpty()) {
            searchSuggestTermMapper.batchInsert(suggestTerms);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(String bizType, Long bizId) {
        searchTermIndexMapper.deleteByBiz(bizType, bizId);
        searchSuggestTermMapper.deleteBySource(bizType, bizId);
        LambdaQueryWrapper<SearchDocIndexDO> wrapper = new LambdaQueryWrapper<SearchDocIndexDO>();
        wrapper.eq(SearchDocIndexDO::getBizType, bizType).eq(SearchDocIndexDO::getBizId, bizId);
        searchDocIndexMapper.delete(wrapper);
    }

    @Override
    public void rebuildAll(String bizType) {
        if (!"quote".equals(bizType)) {
            return;
        }
        long totalRecords = quoteSearchSourceMapper.countActiveQuotes();
        long lastId = 0L;
        long totalSubmitted = 0L;
        int batchNo = 0;
        long startTime = System.currentTimeMillis();
        AtomicLong totalCompleted = new AtomicLong(0L);
        AtomicInteger completedBatchCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        log.info("开始全量刷新搜索索引，业务类型={}, 总量={}, 批大小={}, 并发度={}",
                bizType, totalRecords, REBUILD_BATCH_SIZE, REBUILD_PARALLELISM);
        while (true) {
            List<Long> ids = quoteSearchSourceMapper.selectQuoteIdsAfter(lastId, REBUILD_BATCH_SIZE);
            if (ids == null || ids.isEmpty()) {
                waitAll(futures, totalSubmitted);
                logSummaryProgress(bizType, totalRecords, totalCompleted.get(), completedBatchCount.get(), startTime);
                log.info("全量刷新搜索索引完成，业务类型={}, 总量={}, 累计提交={}, 累计完成={}",
                        bizType, totalRecords, totalSubmitted, totalCompleted.get());
                return;
            }
            batchNo++;
            totalSubmitted += ids.size();
            List<Long> batchIds = new ArrayList<Long>(ids);
            final int currentBatchNo = batchNo;
            final long currentSubmitted = totalSubmitted;
            final Long startId = batchIds.get(0);
            final Long endId = batchIds.get(batchIds.size() - 1);
            log.info("提交全量刷新批次，业务类型={}, 批次={}, 本批数量={}, 起始ID={}, 结束ID={}, 累计提交={}",
                    bizType, currentBatchNo, batchIds.size(), startId, endId, currentSubmitted);
            futures.add(commonTaskExecutor.asyncExecute(() -> {
                rebuildBatchInTransaction(bizType, batchIds, currentBatchNo, currentSubmitted,
                        totalRecords, totalCompleted, completedBatchCount, startTime);
                return null;
            }));
            waitForAvailableSlot(futures, totalSubmitted);
            if (ids.size() < REBUILD_BATCH_SIZE) {
                waitAll(futures, totalSubmitted);
                logSummaryProgress(bizType, totalRecords, totalCompleted.get(), completedBatchCount.get(), startTime);
                log.info("全量刷新搜索索引完成，业务类型={}, 总量={}, 累计提交={}, 累计完成={}",
                        bizType, totalRecords, totalSubmitted, totalCompleted.get());
                return;
            }
            lastId = ids.get(ids.size() - 1);
        }
    }

    private void rebuildBatchInTransaction(String bizType, List<Long> ids, int batchNo, long totalSubmitted,
                                           long totalRecords, AtomicLong totalCompleted,
                                           AtomicInteger completedBatchCount, long startTime) {
        Long startId = ids.isEmpty() ? null : ids.get(0);
        Long endId = ids.isEmpty() ? null : ids.get(ids.size() - 1);
        long batchStartTime = System.currentTimeMillis();
        log.info("开始执行全量刷新批次，业务类型={}, 批次={}, 本批数量={}, 起始ID={}, 结束ID={}",
                bizType, batchNo, ids.size(), startId, endId);
        try {
            transactionTemplate.executeWithoutResult(status -> rebuildBatch(bizType, ids));
            long cost = System.currentTimeMillis() - batchStartTime;
            long completed = totalCompleted.addAndGet(ids.size());
            int completedBatches = completedBatchCount.incrementAndGet();
            log.info("完成全量刷新批次，业务类型={}, 批次={}, 本批数量={}, 起始ID={}, 结束ID={}, 累计提交={}, 累计完成={}, 耗时={}ms",
                    bizType, batchNo, ids.size(), startId, endId, totalSubmitted, completed, cost);
            if (completedBatches % REBUILD_PROGRESS_LOG_BATCH_INTERVAL == 0 || completed >= totalRecords) {
                logSummaryProgress(bizType, totalRecords, completed, completedBatches, startTime);
            }
        } catch (RuntimeException e) {
            long cost = System.currentTimeMillis() - batchStartTime;
            log.error("全量刷新批次失败，业务类型={}, 批次={}, 本批数量={}, 起始ID={}, 结束ID={}, 累计提交={}, 耗时={}ms",
                    bizType, batchNo, ids.size(), startId, endId, totalSubmitted, cost, e);
            throw e;
        }
    }

    protected void rebuildBatch(String bizType, List<Long> ids) {
        List<QuoteSearchSyncData> syncDataList = quoteSearchSourceMapper.selectQuoteSyncDataBatch(ids);
        if (syncDataList == null || syncDataList.isEmpty()) {
            return;
        }

        List<SearchDocIndexDO> docs = new ArrayList<SearchDocIndexDO>(syncDataList.size());
        List<SearchTermIndexDO> terms = new ArrayList<SearchTermIndexDO>();
        List<SearchSuggestTermDO> suggestTerms = new ArrayList<SearchSuggestTermDO>();
        for (QuoteSearchSyncData syncData : syncDataList) {
            SearchDocIndexDO doc = searchDocumentBuilder.build(syncData);
            docs.add(doc);
            terms.addAll(searchTermBuilder.build(doc));
            suggestTerms.addAll(suggestTermBuilder.build(doc));
        }

        searchDocIndexMapper.batchUpsert(docs);
        searchTermIndexMapper.deleteByBizIds(bizType, ids);
        List<SearchTermIndexDO> deduplicatedTerms = deduplicateTerms(terms);
        if (!deduplicatedTerms.isEmpty()) {
            searchTermIndexMapper.batchInsert(deduplicatedTerms);
        }
        searchSuggestTermMapper.deleteBySourceIds(bizType, ids);
        List<SearchSuggestTermDO> deduplicatedSuggestTerms = deduplicateSuggestTerms(suggestTerms);
        if (!deduplicatedSuggestTerms.isEmpty()) {
            searchSuggestTermMapper.batchInsert(deduplicatedSuggestTerms);
        }
    }

    private List<SearchTermIndexDO> deduplicateTerms(List<SearchTermIndexDO> terms) {
        Map<String, SearchTermIndexDO> deduplicated = new LinkedHashMap<String, SearchTermIndexDO>();
        for (SearchTermIndexDO term : terms) {
            String key = term.getBizType() + "_" + term.getBizId() + "_" + term.getTermNormalized()
                    + "_" + term.getTermType() + "_" + term.getSourceField();
            deduplicated.put(key, term);
        }
        return new ArrayList<SearchTermIndexDO>(deduplicated.values());
    }

    private List<SearchSuggestTermDO> deduplicateSuggestTerms(List<SearchSuggestTermDO> terms) {
        Map<String, SearchSuggestTermDO> deduplicated = new LinkedHashMap<String, SearchSuggestTermDO>();
        for (SearchSuggestTermDO term : terms) {
            String key = term.getTermNormalized() + "_" + term.getTermType() + "_" + term.getSourceId();
            deduplicated.put(key, term);
        }
        return new ArrayList<SearchSuggestTermDO>(deduplicated.values());
    }

    private void waitForAvailableSlot(List<CompletableFuture<Void>> futures, long totalSubmitted) {
        while (futures.size() >= REBUILD_PARALLELISM) {
            if (drainCompletedFuture(futures)) {
                log.info("并行批次槽位释放，当前运行中批次数={}, 累计提交={}", futures.size(), totalSubmitted);
                return;
            }
            joinFuture(futures.remove(0));
            log.info("并行批次达到上限，等待最早提交批次完成后继续，当前运行中批次数={}, 累计提交={}",
                    futures.size(), totalSubmitted);
            return;
        }
    }

    private boolean drainCompletedFuture(List<CompletableFuture<Void>> futures) {
        Iterator<CompletableFuture<Void>> iterator = futures.iterator();
        while (iterator.hasNext()) {
            CompletableFuture<Void> future = iterator.next();
            if (future.isDone()) {
                joinFuture(future);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private void waitAll(List<CompletableFuture<Void>> futures, long totalSubmitted) {
        log.info("等待剩余全量刷新批次执行完成，剩余批次数={}, 累计提交={}", futures.size(), totalSubmitted);
        for (CompletableFuture<Void> future : futures) {
            joinFuture(future);
        }
        futures.clear();
    }

    private void logSummaryProgress(String bizType, long totalRecords, long completedRecords,
                                    int completedBatches, long startTime) {
        long elapsedMs = Math.max(System.currentTimeMillis() - startTime, 1L);
        double percent = totalRecords <= 0 ? 100D : completedRecords * 100D / totalRecords;
        long remainingRecords = Math.max(totalRecords - completedRecords, 0L);
        long etaMs = completedRecords <= 0 ? -1L : remainingRecords * elapsedMs / completedRecords;
        log.info("全量刷新整体进度，业务类型={}, 已完成={}/{}, 进度={}%, 已完成批次={}, 已耗时={}, 预计剩余={}",
                bizType, completedRecords, totalRecords, formatPercent(percent), completedBatches,
                formatDuration(elapsedMs), formatDuration(etaMs));
    }

    private String formatPercent(double percent) {
        return String.format("%.2f", percent);
    }

    private String formatDuration(long durationMs) {
        if (durationMs < 0) {
            return "未知";
        }
        long totalSeconds = durationMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return hours + "小时" + minutes + "分钟" + seconds + "秒";
        }
        if (minutes > 0) {
            return minutes + "分钟" + seconds + "秒";
        }
        return seconds + "秒";
    }

    private void joinFuture(CompletableFuture<Void> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("search rebuild interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("search rebuild batch failed", cause);
        }
    }
}
