package org.doubao.recommend.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.recommend.service.domain.FeedResponse;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.service.FeedService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class FeedController {

    @Resource
    private FeedService feedService;

    @GetMapping("/feed")
    public Result<FeedResponse> feed(@RequestParam String userIdentity,
                                     @RequestParam(required = false) String cursor,
                                     @RequestParam(defaultValue = "20") Integer size,
                                     @RequestParam(defaultValue = "home") String scene,
                                     @RequestParam(required = false) Long contentId,
                                     @RequestParam(required = false) Long topicId,
                                     @RequestParam(required = false) String author,
                                     @RequestParam(required = false) Long userId) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setUserIdentity(userIdentity);
        request.setCursor(cursor);
        request.setPageSize(size);
        request.setScene(scene);
        request.setContentId(contentId);
        request.setTopicId(topicId);
        request.setAuthor(author);
        request.setLimit(Math.max(size == null ? 20 : size * 5, 100));
        return Result.success(feedService.getFeed(request));
    }
}
