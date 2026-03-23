package org.doubao.like.service.task;

import org.doubao.like.service.config.HotListProperties;
import org.doubao.like.service.service.HotContentRankManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class HotContentRankRefreshTask {
	@Resource
	private HotContentRankManager hotContentRankManager;

	@Resource
	private HotListProperties hotListProperties;

	@Scheduled(fixedDelayString = "#{@hotListProperties.refreshIntervalMinutes * 60 * 1000}")
	public void refreshHotContentRanks() {
		if (!hotListProperties.isEnabled()) {
			return;
		}
		hotContentRankManager.refreshAllRanks();
	}
}
