package org.doubao.search.service.es.service.remote;

import org.doubao.search.service.es.service.remote.fallback.UserRemoteFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service", fallback = UserRemoteFallback.class)
public interface UserRemoteService {
    @GetMapping("/api/v1/user/{userId}/following")
    List<Long> getUserFollowingIds(@PathVariable("userId") Long userId);

}
