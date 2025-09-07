package org.doubao.search.service.service.remote.fallback;

import org.doubao.search.service.service.remote.UserRemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class UserRemoteFallback implements UserRemoteService {
    private static final Logger log = LoggerFactory.getLogger(UserRemoteFallback.class);

    @Override
    public List<Long> getUserFollowingIds(Long userId) {
        log.warn("获取用户关注列表失败，使用降级策略，userId: {}", userId);
        return Collections.emptyList();
    }
}
