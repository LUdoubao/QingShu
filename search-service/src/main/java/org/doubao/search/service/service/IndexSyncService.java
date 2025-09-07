package org.doubao.search.service.service;


import org.doubao.search.service.model.message.IndexUpdateMessage;

public interface IndexSyncService {
    void handleIndexUpdate(IndexUpdateMessage message);
    
    void syncCopywritingToEs(Long copywritingId);
    
    void deleteCopywritingFromEs(Long copywritingId);
    
    void batchSyncCopywritings(int batchSize, int startPage);
}
