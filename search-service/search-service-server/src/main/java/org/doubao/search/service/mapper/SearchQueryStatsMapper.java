package org.doubao.search.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SearchQueryStatsMapper {

    int upsertStats(@Param("queryText") String queryText,
                    @Param("normalizedQuery") String normalizedQuery,
                    @Param("resultCount") long resultCount);
}
