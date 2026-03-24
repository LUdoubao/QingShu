package org.doubao.search.service.service.impl;

import org.doubao.search.service.entity.SearchHistory;
import org.doubao.search.service.history.SearchHistoryRepository;
import org.doubao.search.service.service.SearchHistoryService;
import org.doubao.search.service.support.QueryPreprocessor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final int HISTORY_LIMIT = 20;

    @Resource
    private SearchHistoryRepository searchHistoryRepository;

    @Resource
    private QueryPreprocessor queryPreprocessor;

    @Override
    public void saveSearchHistory(Long userId, String keyword) {
        searchHistoryRepository.save(userId, keyword, queryPreprocessor.normalize(keyword));
    }

    @Override
    public List<SearchHistory> getUserSearchHistory(Long userId) {
        return searchHistoryRepository.findUserHistory(userId, HISTORY_LIMIT);
    }

    @Override
    public boolean clearUserSearchHistory(Long userId) {
        return searchHistoryRepository.clearUserHistory(userId);
    }
}
