package org.doubao.feed.service.feign;

import java.util.List;

public interface UserBehaviorClient {
	int countInteractions(Long userId, Long actorId);

	boolean checkMutualFollow(Long userId, Long actorId);

	List<String> getUserInterestTags(Long userId);
}
