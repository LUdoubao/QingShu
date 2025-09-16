package org.doubao.search.service.es.repository.elasticsearch;

import org.doubao.search.service.es.model.entity.es.CopywritingEsEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CopywritingEsRepository extends ElasticsearchRepository<CopywritingEsEntity, Long> {
}
