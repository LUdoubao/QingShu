package org.doubao.search.service.db.service;

import org.doubao.search.service.db.entity.SearchHistory;
import java.util.List;

public interface SearchHistoryService {
    /**
     * 保存搜索历史
     */
    void saveSearchHistory(Long userId, String keyword);
    
    /**
     * 获取用户搜索历史
     */
    List<SearchHistory> getUserSearchHistory(Long userId);
    
    /**
     * 清空用户搜索历史
     */
    boolean clearUserSearchHistory(Long userId);
}
