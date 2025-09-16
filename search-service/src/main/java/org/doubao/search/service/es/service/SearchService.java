package org.doubao.search.service.es.service;

import org.doubao.search.service.es.model.dto.SearchRequestDto;
import org.doubao.search.service.es.model.vo.HotSearchVO;
import org.doubao.search.service.es.model.vo.SearchResultVO;

import java.util.List;

public interface SearchService {
    SearchResultVO search(SearchRequestDto request, Long userId);
    
    List<HotSearchVO> getHotSearches(String period, Integer limit);
    
    void recordSearchStatistics(String keyword, Long userId, Integer resultCount);
}
