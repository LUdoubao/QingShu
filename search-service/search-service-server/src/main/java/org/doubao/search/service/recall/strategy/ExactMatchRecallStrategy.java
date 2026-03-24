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
public class ExactMatchRecallStrategy implements RecallStrategy {

    @Resource
    private SearchDocIndexMapper searchDocIndexMapper;
    @Resource
    private SearchDocConverter searchDocConverter;

    @Override
    public RecallResult recall(QueryContext context) {
        List<SearchDocIndexDO> matches = searchDocIndexMapper.selectExactMatches("quote", context.getNormalizedQuery(), 50);
        List<RecallDoc> docs = new ArrayList<RecallDoc>();
        for (SearchDocIndexDO match : matches) {
            docs.add(new RecallDoc(match.getBizId(), searchDocConverter.toResult(match), "EXACT", 100D));
        }
        return new RecallResult(docs.size(), docs);
    }
}
