package org.doubao.search.service.history.impl;

import org.doubao.search.service.entity.SearchHistory;
import org.doubao.search.service.entity.UserSearchHistoryDO;
import org.doubao.search.service.history.SearchHistoryRepository;
import org.doubao.search.service.mapper.UserSearchHistoryMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MybatisSearchHistoryRepository implements SearchHistoryRepository {

    @Resource
    private UserSearchHistoryMapper userSearchHistoryMapper;

    @Override
    @Transactional
    public void save(Long userId, String keyword, String normalizedKeyword) {
        if (userId == null || keyword == null || keyword.trim().isEmpty() || normalizedKeyword == null || normalizedKeyword.isEmpty()) {
            return;
        }
        userSearchHistoryMapper.upsertHistory(userId, keyword.trim(), normalizedKeyword);
    }

    @Override
    public List<SearchHistory> findUserHistory(Long userId, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        List<UserSearchHistoryDO> historyList = userSearchHistoryMapper.selectUserHistory(userId, limit);
        List<SearchHistory> result = new ArrayList<>();
        for (UserSearchHistoryDO item : historyList) {
            SearchHistory history = new SearchHistory();
            history.setId(item.getId());
            history.setUserId(item.getUserId());
            history.setKeyword(item.getQueryText());
            history.setCreatedTime(item.getLastSearchTime() == null ? LocalDateTime.now() : item.getLastSearchTime());
            result.add(history);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean clearUserHistory(Long userId) {
        if (userId == null) {
            return false;
        }
        return userSearchHistoryMapper.clearUserHistory(userId) >= 0;
    }
}
