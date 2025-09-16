package org.doubao.search.service.es.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.doubao.search.service.es.mapper.SearchHistoryMapper;
import org.doubao.search.service.es.model.document.UserSearchHistoryDocument;
import org.doubao.search.service.es.model.entity.SearchHistory;
import org.doubao.search.service.es.model.vo.SearchHistoryVO;
import org.doubao.search.service.es.repository.mongodb.UserSearchHistoryRepository;
import org.doubao.search.service.es.service.SearchHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.bson.types.ObjectId;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final Logger log = LoggerFactory.getLogger(SearchHistoryServiceImpl.class);
    @Resource
    private SearchHistoryMapper searchHistoryMapper;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private UserSearchHistoryRepository userSearchHistoryRepository;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${search.history.max-count}")
    private Integer maxHistoryCount;

    @Override
    public List<SearchHistoryVO> getUserSearchHistory(Long userId, Integer limit) {
        if (userId == null || userId <= 0) {
            return Collections.emptyList();
        }
        
        // 限制查询数量
        int queryLimit = limit != null && limit > 0 ? Math.min(limit, maxHistoryCount) : maxHistoryCount;
        
        // 尝试从Redis获取
        String cacheKey = "search:history:user:" + userId;
        List<SearchHistoryVO> cachedHistory = (List<SearchHistoryVO>) redisTemplate.opsForValue().get(cacheKey);
        if (CollUtil.isNotEmpty(cachedHistory)) {
            return cachedHistory.stream().limit(queryLimit).collect(Collectors.toList());
        }
        
        // 从MongoDB查询详细历史
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "searchTime"))
                .limit(queryLimit);
        
        List<UserSearchHistoryDocument> historyDocs = mongoTemplate.find(query, UserSearchHistoryDocument.class);
        
        // 转换为VO
        List<SearchHistoryVO> historyVOs = historyDocs.stream()
                .map(doc -> {
                    SearchHistoryVO vo = new SearchHistoryVO();
                    vo.setId(doc.getId());
                    vo.setKeyword(doc.getKeyword());
                    vo.setSearchTime(doc.getSearchTime());
                    vo.setResultCount(doc.getResultCount());
                    vo.setFilters(doc.getFilters());
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 缓存到Redis
        redisTemplate.opsForValue().set(cacheKey, historyVOs, 7, TimeUnit.DAYS);
        
        return historyVOs;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void saveSearchHistory(Long userId, String keyword, Map<String, Object> filters, Integer resultCount) {
        if (userId == null || userId <= 0 || StrUtil.isBlank(keyword)) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        try {
            // 1. 保存到MySQL（简要记录）
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword);
            history.setSearchTime(now);
            history.setResultCount(resultCount);
            history.setIsDeleted(0);
            history.setCreateTime(now);
            history.setUpdateTime(now);
            searchHistoryMapper.insert(history);
            
            // 2. 保存到MongoDB（详细记录）
            UserSearchHistoryDocument doc = new UserSearchHistoryDocument();
            doc.setUserId(userId);
            doc.setKeyword(keyword);
            doc.setSearchTime(now);
            doc.setFilters(filters);
            doc.setResultCount(resultCount);
            mongoTemplate.save(doc);
            
            // 3. 清理超出数量的历史记录
            cleanOversizedHistory(userId);
            
            // 4. 清除缓存，下次查询重新加载
            String cacheKey = "search:history:user:" + userId;
            redisTemplate.delete(cacheKey);
            
        } catch (Exception e) {
            log.error("保存搜索历史失败", e);
        }
    }

    @Override
    public boolean deleteSearchHistory(Long userId, String historyId) {
        if (userId == null || userId <= 0 || StrUtil.isBlank(historyId)) {
            return false;
        }
        
        try {
            // 1. 从MongoDB删除
            ObjectId objectId;
            try {
                objectId = new ObjectId(historyId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid history ID: {}", historyId);
                return false;
            }
            
            Query query = new Query(
                Criteria.where("_id").is(objectId)
                    .and("userId").is(userId)
            );
            
            long deletedCount = mongoTemplate.remove(query, UserSearchHistoryDocument.class).getDeletedCount();
            
            // 2. 清除缓存
            String cacheKey = "search:history:user:" + userId;
            redisTemplate.delete(cacheKey);
            
            return deletedCount > 0;
        } catch (Exception e) {
            log.error("删除搜索历史失败", e);
            return false;
        }
    }

    @Override
    public boolean clearSearchHistory(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        
        try {
            // 1. 从MongoDB删除所有记录
            Query query = new Query(Criteria.where("userId").is(userId));
            mongoTemplate.remove(query, UserSearchHistoryDocument.class);
            
            // 2. 从MySQL删除所有记录
            searchHistoryMapper.deleteByUserId(userId);
            
            // 3. 清除缓存
            String cacheKey = "search:history:user:" + userId;
            redisTemplate.delete(cacheKey);
            
            return true;
        } catch (Exception e) {
            log.error("清空搜索历史失败", e);
            return false;
        }
    }
    
    /**
     * 清理超出数量限制的历史记录
     */
    private void cleanOversizedHistory(Long userId) {
        // 查询总数量
        long totalCount = mongoTemplate.count(
            new Query(Criteria.where("userId").is(userId)), 
            UserSearchHistoryDocument.class
        );
        
        // 如果超出最大数量，删除最早的记录
        if (totalCount > maxHistoryCount) {
            int excess = (int) (totalCount - maxHistoryCount);
            
            // 查询最早的记录ID
            Query query = new Query(Criteria.where("userId").is(userId))
                    .with(Sort.by(Sort.Direction.ASC, "searchTime"))
                    .limit(excess);
            
            List<UserSearchHistoryDocument> oldestDocs = mongoTemplate.find(query, UserSearchHistoryDocument.class);
            if (CollUtil.isNotEmpty(oldestDocs)) {
                List<ObjectId> ids = oldestDocs.stream()
                        .map(doc -> new ObjectId(doc.getId()))
                        .collect(Collectors.toList());
                
                // 删除最早的记录
                Query deleteQuery = new Query(Criteria.where("_id").in(ids));
                mongoTemplate.remove(deleteQuery, UserSearchHistoryDocument.class);
                
                log.info("Cleaned {} old search history records for user: {}", excess, userId);
            }
        }
    }
}
