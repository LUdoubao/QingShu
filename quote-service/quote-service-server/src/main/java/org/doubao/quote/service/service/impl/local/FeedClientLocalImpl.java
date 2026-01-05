package org.doubao.quote.service.service.impl.local;

import org.doubao.feed.service.service.UserTimelineService;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.UpdateValidDto;
import org.doubao.quote.service.feign.FeedClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class FeedClientLocalImpl implements FeedClient {
	@Resource
	private UserTimelineService userTimelineService;

	@Override
	public Result<Void> updateFeedStatus(UpdateValidDto updateValidDto) {
		org.doubao.feed.service.model.dto.UpdateValidDto feedUpdateValidDto = new org.doubao.feed.service.model.dto.UpdateValidDto();
		feedUpdateValidDto.setActorId(updateValidDto.getActorId());
		feedUpdateValidDto.setTargetId(updateValidDto.getTargetId());
		feedUpdateValidDto.setTargetType(updateValidDto.getTargetType());
		feedUpdateValidDto.setIsValid(updateValidDto.getIsValid());
		userTimelineService.updateValid(feedUpdateValidDto);
		return Result.success();
	}
}