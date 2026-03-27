package org.doubao.interview.agent.server.controller.redis.q025;

import org.doubao.interview.agent.api.dto.redis.q025.RedisDistributedLockRequest;
import org.doubao.interview.agent.api.dto.redis.q025.RedisDistributedLockResponse;
import org.doubao.interview.agent.api.service.redis.q025.RedisDistributedLockDemoService;
import org.doubao.interview.agent.api.service.redis.q025.RedisDistributedLockRedisService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 闂025锛氬垎甯冨紡閿佹帶鍒跺櫒銆?*/
@RestController
@RequestMapping("/interview-agent/questions/q025-distributed-lock")
public class RedisDistributedLockDemoController {

    private final RedisDistributedLockDemoService demoService;
    private final RedisDistributedLockRedisService redisService;

    public RedisDistributedLockDemoController(RedisDistributedLockDemoService demoService,
                                              RedisDistributedLockRedisService redisService) {
        this.demoService = demoService;
        this.redisService = redisService;
    }

    /** 杩愯鍒嗗竷寮忛攣绀轰緥銆?*/
    @PostMapping("/run")
    public RedisDistributedLockResponse run(@RequestBody RedisDistributedLockRequest request) {
        return demoService.run(request);
    }

    /** 杩愯 Redis 鐪熷疄鐗堟湰鍒嗗竷寮忛攣绀轰緥銆?*/
    @PostMapping("/run-redis")
    public RedisDistributedLockResponse runRedis(@RequestBody RedisDistributedLockRequest request) {
        return redisService.runRedis(request);
    }
}
