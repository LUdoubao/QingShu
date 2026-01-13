package org.doubao.feed.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.feed.service.mapper.EventTimelineMapper;
import org.doubao.feed.service.model.entity.EventTimeline;
import org.doubao.feed.service.service.EventTimelineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EventTimelineServiceImpl extends ServiceImpl<EventTimelineMapper, EventTimeline> implements EventTimelineService {

	@Resource
	private EventTimelineMapper eventTimelineMapper;

	/**
	 * 批量插入大V事件
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean batchInsert(List<EventTimeline> list) {
		if (list.isEmpty()) {
			return true;
		}

		// 分批次插入，每批500条
		int batchSize = 500;
		for (int i = 0; i < list.size(); i += batchSize) {
			int end = Math.min(i + batchSize, list.size());
			List<EventTimeline> batch = list.subList(i, end);
			eventTimelineMapper.batchInsert(batch);
		}
		return true;
	}
}