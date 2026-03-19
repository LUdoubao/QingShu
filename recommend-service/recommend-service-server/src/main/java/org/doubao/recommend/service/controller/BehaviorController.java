package org.doubao.recommend.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.recommend.service.domain.BehaviorEvent;
import org.doubao.recommend.service.service.BehaviorService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/behavior")
public class BehaviorController {

    @Resource
    private BehaviorService behaviorService;

    @PostMapping("/record")
    public Result<Void> record(@RequestBody BehaviorEvent event) {
        if (event.getCreatedTime() == null) {
            event.setCreatedTime(LocalDateTime.now());
        }
        behaviorService.record(event);
        return Result.success(null);
    }

    @PostMapping("/view")
    public Result<Void> view(@RequestBody BehaviorEvent event) {
        event.setActionType("view");
        behaviorService.record(event);
        return Result.success(null);
    }

    @PostMapping("/like")
    public Result<Void> like(@RequestBody BehaviorEvent event) {
        event.setActionType("like");
        behaviorService.record(event);
        return Result.success(null);
    }

    @PostMapping("/favorite")
    public Result<Void> favorite(@RequestBody BehaviorEvent event) {
        event.setActionType("favorite");
        behaviorService.record(event);
        return Result.success(null);
    }

    @PostMapping("/comment")
    public Result<Void> comment(@RequestBody BehaviorEvent event) {
        event.setActionType("comment");
        behaviorService.record(event);
        return Result.success(null);
    }
}
