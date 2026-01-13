package org.doubao.feed.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.feed.service.model.entity.EventTimeline;

import java.util.List;

public interface EventTimelineService extends IService<EventTimeline> {
	boolean batchInsert(List<EventTimeline> list);
}