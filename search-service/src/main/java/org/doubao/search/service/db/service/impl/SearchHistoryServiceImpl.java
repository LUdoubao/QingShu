package org.doubao.search.service.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.doubao.search.service.db.entity.SearchHistory;
import org.doubao.search.service.db.service.SearchHistoryService;
import org.doubao.search.service.db.mapper.SearchHistoryMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;
    
    // 最多保存的历史记录数量
    private static final int HISTORY_LIMIT = 20;

    @Override
    @Transactional
    public void saveSearchHistory(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword.trim());
        history.setCreatedTime(LocalDateTime.now());

        searchHistoryMapper.insert(history);
    }

    @Override
    public List<SearchHistory> getUserSearchHistory(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return searchHistoryMapper.getUserSearchHistory(userId, HISTORY_LIMIT);
    }

    @Override
    @Transactional
    public boolean clearUserSearchHistory(Long userId) {
        if (userId == null) {
            return false;
        }
        // 逻辑删除
        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SearchHistory::getUserId, userId);
        return searchHistoryMapper.delete(queryWrapper) > 0;
    }
}
