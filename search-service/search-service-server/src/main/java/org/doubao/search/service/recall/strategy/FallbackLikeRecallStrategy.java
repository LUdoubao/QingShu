package org.doubao.search.service.recall.strategy;

import org.doubao.search.service.converter.SearchDocConverter;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.domain.result.RecallResult;
import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.mapper.SearchDocIndexMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class FallbackLikeRecallStrategy implements RecallStrategy {

    private static final int MAX_LIKE_FALLBACK_WINDOW = 50;

    @Resource
    private SearchDocIndexMapper searchDocIndexMapper;
    @Resource
    private SearchDocConverter searchDocConverter;

    @Override
    public RecallResult recall(QueryContext context) {
        if (!shouldUseLikeFallback(context)) {
            return new RecallResult(0, new ArrayList<RecallDoc>());
        }
        long total = searchDocIndexMapper.countLikeMatches("quote", context.getNormalizedQuery());
        List<SearchDocIndexDO> matches = searchDocIndexMapper.selectLikeMatches("quote",
                context.getNormalizedQuery(), Math.min(context.getRecallWindowSize(), MAX_LIKE_FALLBACK_WINDOW));
        List<RecallDoc> docs = new ArrayList<RecallDoc>();
        for (SearchDocIndexDO match : matches) {
            docs.add(new RecallDoc(match.getBizId(), searchDocConverter.toResult(match), "LIKE", 20D));
        }
        return new RecallResult(total, docs);
    }

    private boolean shouldUseLikeFallback(QueryContext context) {
        String normalizedQuery = context.getNormalizedQuery();
        return normalizedQuery != null
                && normalizedQuery.length() >= 2
                && context.getOffset() < MAX_LIKE_FALLBACK_WINDOW;
    }
}
