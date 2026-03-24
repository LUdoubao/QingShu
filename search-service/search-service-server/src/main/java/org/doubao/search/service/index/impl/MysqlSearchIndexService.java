package org.doubao.search.service.index.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class MysqlSearchIndexService implements SearchIndexService {

    private static final int REBUILD_BATCH_SIZE = 200;

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
        int offset = 0;
        while (true) {
            List<Long> ids = quoteSearchSourceMapper.selectQuoteIds(offset, REBUILD_BATCH_SIZE);
            if (ids == null || ids.isEmpty()) {
                return;
            }
            for (Long id : ids) {
                upsertDocument("quote", id);
            }
            if (ids.size() < REBUILD_BATCH_SIZE) {
                return;
            }
            offset += REBUILD_BATCH_SIZE;
        }
    }
}
