package org.doubao.recommend.service.service;

import org.doubao.recommend.service.domain.FeedResponse;
import org.doubao.recommend.service.domain.RecommendItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class RecommendService {

    @Resource
    private FeedService feedService;

    public List<RecommendItem> recommend(RecommendRequest request) {
        FeedResponse response = feedService.getFeed(request);
        return response == null ? Collections.emptyList() : response.getItems();
    }

    public List<RecommendItem> related(Long contentId, RecommendRequest request) {
        request.setScene("detail");
        request.setContentId(contentId);
        return recommend(request);
    }
}
