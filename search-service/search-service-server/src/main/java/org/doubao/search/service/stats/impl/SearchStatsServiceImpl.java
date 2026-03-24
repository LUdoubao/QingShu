package org.doubao.search.service.stats.impl;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.mapper.SearchQueryStatsMapper;
import org.doubao.search.service.stats.SearchStatsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SearchStatsServiceImpl implements SearchStatsService {

    @Resource
    private SearchQueryStatsMapper searchQueryStatsMapper;

    @Override
    public void recordSearch(QueryContext context, long resultCount) {
        if (context.getNormalizedQuery() == null || context.getNormalizedQuery().isEmpty()) {
            return;
        }
        searchQueryStatsMapper.upsertStats(context.getRawQuery(), context.getNormalizedQuery(), resultCount);
    }
}
