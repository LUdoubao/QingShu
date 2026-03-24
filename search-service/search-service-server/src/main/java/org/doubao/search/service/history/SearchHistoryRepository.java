package org.doubao.search.service.history;

import org.doubao.search.service.entity.SearchHistory;

import java.util.List;

public interface SearchHistoryRepository {

    void save(Long userId, String keyword, String normalizedKeyword);

    List<SearchHistory> findUserHistory(Long userId, int limit);

    boolean clearUserHistory(Long userId);
}
