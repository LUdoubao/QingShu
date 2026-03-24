package org.doubao.search.service.recall.strategy;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.domain.result.RecallResult;
import org.doubao.search.service.entity.SearchCandidateDO;
import org.doubao.search.service.mapper.SearchTermIndexMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrefixRecallStrategy implements RecallStrategy {

    @Resource
    private SearchTermIndexMapper searchTermIndexMapper;

    @Override
    public RecallResult recall(QueryContext context) {
        List<SearchCandidateDO> candidates = searchTermIndexMapper.selectByPrefix("quote", context.getNormalizedQuery(), 100);
        List<RecallDoc> docs = new ArrayList<RecallDoc>();
        for (SearchCandidateDO candidate : candidates) {
            docs.add(new RecallDoc(candidate.getBizId(), null, "PREFIX", candidate.getRecallScore() == null ? 0D : candidate.getRecallScore()));
        }
        return new RecallResult(candidates.size(), docs);
    }
}
