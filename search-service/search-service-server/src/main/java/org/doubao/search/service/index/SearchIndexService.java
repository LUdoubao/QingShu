package org.doubao.search.service.index;

public interface SearchIndexService {

    void upsertDocument(String bizType, Long bizId);

    void deleteDocument(String bizType, Long bizId);

    void rebuildAll(String bizType);
}
