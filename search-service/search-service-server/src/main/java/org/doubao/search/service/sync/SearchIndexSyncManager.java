package org.doubao.search.service.sync;

public interface SearchIndexSyncManager {

    void submitUpsert(String bizType, Long bizId);

    void submitDelete(String bizType, Long bizId);

    void processPendingTasks();
}
