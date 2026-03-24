package org.doubao.search.service.sync.impl;

import org.doubao.search.service.entity.SearchIndexSyncTaskDO;
import org.doubao.search.service.index.SearchIndexService;
import org.doubao.search.service.mapper.SearchIndexSyncTaskMapper;
import org.doubao.search.service.sync.SearchIndexSyncManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DbSearchIndexSyncManager implements SearchIndexSyncManager {

    @Resource
    private SearchIndexSyncTaskMapper searchIndexSyncTaskMapper;
    @Resource
    private SearchIndexService searchIndexService;

    @Override
    @Transactional
    public void submitUpsert(String bizType, Long bizId) {
        SearchIndexSyncTaskDO task = new SearchIndexSyncTaskDO();
        task.setBizType(bizType);
        task.setBizId(bizId);
        task.setOpType("UPSERT");
        task.setTaskStatus("INIT");
        task.setRetryCount(0);
        searchIndexSyncTaskMapper.insert(task);
    }

    @Override
    @Transactional
    public void submitDelete(String bizType, Long bizId) {
        SearchIndexSyncTaskDO task = new SearchIndexSyncTaskDO();
        task.setBizType(bizType);
        task.setBizId(bizId);
        task.setOpType("DELETE");
        task.setTaskStatus("INIT");
        task.setRetryCount(0);
        searchIndexSyncTaskMapper.insert(task);
    }

    @Override
    public void processPendingTasks() {
        List<SearchIndexSyncTaskDO> tasks = searchIndexSyncTaskMapper.selectPendingTasks(100);
        for (SearchIndexSyncTaskDO task : tasks) {
            try {
                if ("DELETE".equals(task.getOpType())) {
                    searchIndexService.deleteDocument(task.getBizType(), task.getBizId());
                } else {
                    searchIndexService.upsertDocument(task.getBizType(), task.getBizId());
                }
                searchIndexSyncTaskMapper.markSuccess(task.getId());
            } catch (Exception ex) {
                searchIndexSyncTaskMapper.markFailed(task.getId(), ex.getMessage());
            }
        }
    }
}
