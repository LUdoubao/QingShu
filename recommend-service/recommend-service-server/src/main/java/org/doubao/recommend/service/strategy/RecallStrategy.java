package org.doubao.recommend.service.strategy;


import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.RecommendRequest;

import java.util.List;

public interface RecallStrategy {
    boolean support(RecommendRequest request);
    List<CandidateItem> recall(RecommendRequest request);
}
