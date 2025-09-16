package org.doubao.search.service.es.service;

import org.doubao.search.service.es.model.vo.SearchHistoryVO;

import java.util.List;
import java.util.Map;

public interface SearchHistoryService {
    List<SearchHistoryVO> getUserSearchHistory(Long userId, Integer limit);
    
    void saveSearchHistory(Long userId, String keyword, Map<String, Object> filters, Integer resultCount);
    
    boolean deleteSearchHistory(Long userId, String historyId);
    
    boolean clearSearchHistory(Long userId);
}
