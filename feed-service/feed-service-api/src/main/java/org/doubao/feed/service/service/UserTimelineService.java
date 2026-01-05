package org.doubao.feed.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.feed.service.model.dto.UpdateValidDto;
import org.doubao.feed.service.model.entity.UserTimeline;

import java.util.List;

public interface UserTimelineService extends IService<UserTimeline> {
	void batchInsert(List<UserTimeline> list);

	void updateValid(UpdateValidDto updateValidDto);
}