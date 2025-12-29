package org.doubao.quote.service.repository;

import org.doubao.quote.service.entity.CitationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CitationRepository extends MongoRepository<CitationDocument, String> {

	// 根据SimHash查找相似引文
	List<CitationDocument> findByContentSimHash(long[] contentSimHash);

	// 根据作者查找引文
	List<CitationDocument> findByAuthor(String author);

	// 根据n-gram查找引文
	@Query("{ 'trigrams': { $in: ?0 } }")
	List<CitationDocument> findByTrigramsIn(Set<String> trigrams);

	// 根据原创性查找引文
	List<CitationDocument> findByIsOriginal(boolean isOriginal);
}
