package org.doubao.search.service.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.search.service.entity.QuoteSearchSyncData;

import java.util.List;

@Mapper
public interface QuoteSearchSourceMapper {

    long countActiveQuotes();

    List<Long> selectQuoteIdsAfter(@Param("lastId") Long lastId, @Param("limit") int limit);

    QuoteSearchSyncData selectQuoteSyncData(@Param("quoteId") Long quoteId);

    List<QuoteSearchSyncData> selectQuoteSyncDataBatch(@Param("quoteIds") List<Long> quoteIds);
}
