package org.doubao.search.service.recall;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.domain.result.RecallResult;

import java.util.List;

public interface RecallService {

    RecallResult recall(QueryContext context);
}
