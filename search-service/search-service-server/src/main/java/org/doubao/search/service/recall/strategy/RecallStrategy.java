package org.doubao.search.service.recall.strategy;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallResult;

public interface RecallStrategy {

    RecallResult recall(QueryContext context);
}
