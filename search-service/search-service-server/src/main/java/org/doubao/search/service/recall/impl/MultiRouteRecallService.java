package org.doubao.search.service.recall.impl;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.domain.result.RecallResult;
import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.mapper.SearchDocIndexMapper;
import org.doubao.search.service.converter.SearchDocConverter;
import org.doubao.search.service.recall.RecallService;
import org.doubao.search.service.recall.strategy.ExactMatchRecallStrategy;
import org.doubao.search.service.recall.strategy.FallbackLikeRecallStrategy;
import org.doubao.search.service.recall.strategy.PrefixRecallStrategy;
import org.doubao.search.service.recall.strategy.RemoteQuoteRecallStrategy;
import org.doubao.search.service.recall.strategy.TermRecallStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MultiRouteRecallService implements RecallService {

    @Resource
    private ExactMatchRecallStrategy exactMatchRecallStrategy;
    @Resource
    private PrefixRecallStrategy prefixRecallStrategy;
    @Resource
    private TermRecallStrategy termRecallStrategy;
    @Resource
    private FallbackLikeRecallStrategy fallbackLikeRecallStrategy;
    @Resource
    private RemoteQuoteRecallStrategy remoteQuoteRecallStrategy;
    @Resource
    private SearchDocIndexMapper searchDocIndexMapper;
    @Resource
    private SearchDocConverter searchDocConverter;

    @Override
    public RecallResult recall(QueryContext context) {
        Map<Long, RecallDoc> merged = new LinkedHashMap<Long, RecallDoc>();
        RecallResult exactResult = exactMatchRecallStrategy.recall(context);
        RecallResult prefixResult = prefixRecallStrategy.recall(context);
        RecallResult termResult = termRecallStrategy.recall(context);
        merge(merged, exactResult.getDocs());
        merge(merged, prefixResult.getDocs());
        merge(merged, termResult.getDocs());
        long total = merged.size();
        if (merged.size() < 20) {
            RecallResult likeResult = fallbackLikeRecallStrategy.recall(context);
            merge(merged, likeResult.getDocs());
            total = Math.max(total, likeResult.getTotal());
        }
        if (merged.isEmpty() || merged.size() <= context.getOffset()) {
            return remoteQuoteRecallStrategy.recall(context);
        }
        hydrateDocuments(merged);
        pruneMissingDocuments(merged);
        total = Math.max(total, merged.size());
        return new RecallResult(total, new ArrayList<RecallDoc>(merged.values()));
    }

    private void merge(Map<Long, RecallDoc> merged, List<RecallDoc> docs) {
        for (RecallDoc doc : docs) {
            RecallDoc existing = merged.get(doc.getBizId());
            if (existing == null || doc.getScore() > existing.getScore()) {
                merged.put(doc.getBizId(), doc);
            }
        }
    }

    private void hydrateDocuments(Map<Long, RecallDoc> merged) {
        List<Long> bizIds = new ArrayList<Long>(merged.keySet());
        List<SearchDocIndexDO> docs = searchDocIndexMapper.selectByBizIds("quote", bizIds);
        for (SearchDocIndexDO doc : docs) {
            RecallDoc recallDoc = merged.get(doc.getBizId());
            if (recallDoc != null && recallDoc.getDocument() == null) {
                recallDoc.setDocument(searchDocConverter.toResult(doc));
            }
        }
    }

    private void pruneMissingDocuments(Map<Long, RecallDoc> merged) {
        Iterator<Map.Entry<Long, RecallDoc>> iterator = merged.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, RecallDoc> entry = iterator.next();
            if (entry.getValue().getDocument() == null) {
                iterator.remove();
            }
        }
    }
}
