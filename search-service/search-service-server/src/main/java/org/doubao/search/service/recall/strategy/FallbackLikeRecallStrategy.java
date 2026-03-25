package org.doubao.search.service.recall.strategy;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FallbackLikeRecallStrategy implements RecallStrategy {

    @Override
    public RecallResult recall(QueryContext context) {
        return new RecallResult(0, new ArrayList<>());
    }
}
