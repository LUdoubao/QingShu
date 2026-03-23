package org.doubao.like.service.task;

import org.doubao.like.service.service.HotContentRankManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class HotContentRankPreloadRunner implements ApplicationRunner {
	@Resource
	private HotContentRankManager hotContentRankManager;

	@Override
	public void run(ApplicationArguments args) {
		if (!hotContentRankManager.isEnabled() || !hotContentRankManager.shouldPreloadOnStartup()) {
			return;
		}
		hotContentRankManager.refreshAllRanks();
	}
}
