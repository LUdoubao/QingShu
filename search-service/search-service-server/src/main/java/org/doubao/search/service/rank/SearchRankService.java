package org.doubao.search.service.rank;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;

import java.util.List;

public interface SearchRankService {

    List<RecallDoc> rank(QueryContext context, List<RecallDoc> docs);
}
