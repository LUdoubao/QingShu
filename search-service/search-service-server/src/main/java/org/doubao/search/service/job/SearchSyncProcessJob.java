package org.doubao.search.service.job;

import org.doubao.search.service.sync.SearchIndexSyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SearchSyncProcessJob {

    private static final Logger LOG = LoggerFactory.getLogger(SearchSyncProcessJob.class);

    @Resource
    private SearchIndexSyncManager searchIndexSyncManager;

    @Scheduled(fixedDelayString = "${search.sync.process-delay-ms:5000}", initialDelayString = "${search.sync.initial-delay-ms:10000}")
    public void processPendingTasks() {
        try {
            searchIndexSyncManager.processPendingTasks();
        } catch (Exception ex) {
            LOG.error("[search-sync-job] process pending tasks failed", ex);
        }
    }
}
