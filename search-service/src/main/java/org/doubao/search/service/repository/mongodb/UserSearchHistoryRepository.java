package org.doubao.search.service.repository.mongodb;

import org.doubao.search.service.model.document.UserSearchHistoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UserSearchHistoryRepository extends MongoRepository<UserSearchHistoryDocument, String> {
    List<UserSearchHistoryDocument> findByUserIdOrderBySearchTimeDesc(Long userId);
    void deleteByUserId(Long userId);
}
