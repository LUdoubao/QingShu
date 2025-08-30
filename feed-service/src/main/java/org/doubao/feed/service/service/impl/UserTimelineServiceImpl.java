package org.doubao.feed.service.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.feed.service.mapper.UserTimelineMapper;
import org.doubao.feed.service.model.entity.UserTimeline;
import org.doubao.feed.service.service.UserTimelineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserTimelineServiceImpl extends ServiceImpl<UserTimelineMapper, UserTimeline> implements UserTimelineService {

	@Resource
	private UserTimelineMapper userTimelineMapper;

	/**
	 * 批量插入用户时间线
	 */
	@Transactional(rollbackFor = Exception.class)
	public void batchInsert(List<UserTimeline> list) {
		if (list.isEmpty()) {
			return;
		}

		// 分批次插入，每批500条
		int batchSize = 500;
		for (int i = 0; i < list.size(); i += batchSize) {
			int end = Math.min(i + batchSize, list.size());
			List<UserTimeline> batch = list.subList(i, end);
			this.saveBatch(batch);
		}
	}
}
