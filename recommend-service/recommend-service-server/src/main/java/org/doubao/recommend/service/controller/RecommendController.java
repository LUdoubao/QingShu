package org.doubao.recommend.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.recommend.service.domain.FeedResponse;
import org.doubao.recommend.service.domain.RecommendItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.service.FeedService;
import org.doubao.recommend.service.service.RecommendService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Resource
    private RecommendService recommendService;
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
    @GetMapping("/home")
    public Result<FeedResponse> home(@RequestParam(required = false) Long userId,
                                     @RequestParam(required = false) String userIdentity,
                                     @RequestParam(required = false) String cursor,
                                     @RequestParam(defaultValue = "20") Integer size,
                                     @RequestParam(defaultValue = "home") String scene) {
        RecommendRequest request = baseRequest(userId, userIdentity, scene, cursor, size);
        return Result.success(feedService.getFeed(request));
    }

    @GetMapping("/related/{contentId}")
    public Result<List<RecommendItem>> related(@PathVariable Long contentId,
                                               @RequestParam(required = false) Long userId,
                                               @RequestParam(required = false) String userIdentity) {
        RecommendRequest request = baseRequest(userId, userIdentity, "detail", null, 20);
        request.setContentId(contentId);
        return Result.success(recommendService.related(contentId, request));
    }

    @GetMapping("/topic/{topicId}")
    public Result<FeedResponse> byTopic(@PathVariable Long topicId,
                                        @RequestParam(required = false) Long userId,
                                        @RequestParam(required = false) String userIdentity,
                                        @RequestParam(required = false) String cursor,
                                        @RequestParam(defaultValue = "20") Integer size) {
        RecommendRequest request = baseRequest(userId, userIdentity, "topic", cursor, size);
        request.setTopicId(topicId);
        return Result.success(feedService.getFeed(request));
    }

    @GetMapping("/guess")
    public Result<FeedResponse> guess(@RequestParam(required = false) Long userId,
                                      @RequestParam(required = false) String userIdentity,
                                      @RequestParam(required = false) String cursor,
                                      @RequestParam(defaultValue = "20") Integer size) {
        RecommendRequest request = baseRequest(userId, userIdentity, "guess", cursor, size);
        return Result.success(feedService.getFeed(request));
    }

    @GetMapping("/author")
    public Result<FeedResponse> byAuthor(@RequestParam String author,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String userIdentity,
                                         @RequestParam(required = false) String cursor,
                                         @RequestParam(defaultValue = "20") Integer size) {
        RecommendRequest request = baseRequest(userId, userIdentity, "author", cursor, size);
        request.setAuthor(author);
        return Result.success(feedService.getFeed(request));
    }

    private RecommendRequest baseRequest(Long userId, String userIdentity, String scene, String cursor, Integer size) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setUserIdentity(userIdentity);
        request.setScene(scene);
        request.setCursor(cursor);
        request.setPageSize(size);
        request.setLimit(Math.max(size == null ? 20 : size * 5, 100));
        return request;
    }
}
