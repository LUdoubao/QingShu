package org.doubao.search.service.stats;

import org.doubao.search.service.domain.query.QueryContext;

public interface SearchStatsService {

    void recordSearch(QueryContext context, long resultCount);
}
