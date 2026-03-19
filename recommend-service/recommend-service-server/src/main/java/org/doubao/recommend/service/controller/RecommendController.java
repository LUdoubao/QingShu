package org.doubao.recommend.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.recommend.service.domain.RecommendItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.service.RecommendService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Resource
    private RecommendService recommendService;

    @GetMapping("/home")
    public Result<List<RecommendItem>> home(@RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) String userIdentity,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "20") Integer pageSize) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setUserIdentity(userIdentity);
        request.setScene("home");
        request.setPage(page);
        request.setPageSize(pageSize);
        return Result.success(recommendService.recommend(request));
    }

    @GetMapping("/related/{contentId}")
    public Result<List<RecommendItem>> related(@PathVariable Long contentId,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) String userIdentity) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setUserIdentity(userIdentity);
        request.setScene("detail");
        request.setContentId(contentId);
        request.setPage(1);
        request.setPageSize(20);
        return Result.success(recommendService.related(contentId, request));
    }

    @GetMapping("/topic/{topicId}")
    public Result<List<RecommendItem>> byTopic(@PathVariable Long topicId,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) String userIdentity) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setUserIdentity(userIdentity);
        request.setScene("topic");
        request.setTopicId(topicId);
        request.setPage(1);
        request.setPageSize(20);
        return Result.success(recommendService.recommend(request));
    }


    @GetMapping("/guess")
    public Result<List<RecommendItem>> guess(@RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) String userIdentity) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setUserIdentity(userIdentity);
        request.setScene("guess");
        request.setPage(1);
        request.setPageSize(20);
        return Result.success(recommendService.recommend(request));
    }

    @GetMapping("/author")
    public Result<List<RecommendItem>> byAuthor(@RequestParam String author,
                                                     @RequestParam(required = false) Long userId,
                                                     @RequestParam(required = false) String userIdentity) {
        RecommendRequest request = new RecommendRequest();
        request.setUserId(userId);
        request.setUserIdentity(userIdentity);
        request.setScene("author");
        request.setAuthor(author);
        request.setPage(1);
        request.setPageSize(20);
        return Result.success(recommendService.recommend(request));
    }
}
