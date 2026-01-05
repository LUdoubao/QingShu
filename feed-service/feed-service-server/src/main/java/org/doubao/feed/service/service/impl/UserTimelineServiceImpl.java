package org.doubao.feed.service.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.feed.service.mapper.UserTimelineMapper;
import org.doubao.feed.service.model.dto.UpdateValidDto;
import org.doubao.feed.service.model.entity.UserTimeline;
import org.doubao.feed.service.service.UserTimelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserTimelineServiceImpl extends ServiceImpl<UserTimelineMapper, UserTimeline> implements UserTimelineService {

	@Resource
	private UserTimelineMapper userTimelineMapper;

	private static final Logger logger = LoggerFactory.getLogger(UserTimelineServiceImpl.class);
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
			userTimelineMapper.batchInsertIgnore(batch);
		}
	}

	@Override
	public void updateValid(UpdateValidDto updateValidDto) {
		Integer isValid = updateValidDto.getIsValid();
		Long actorId = updateValidDto.getActorId();
		Long targetId = updateValidDto.getTargetId();
		String targetType = updateValidDto.getTargetType();
		LambdaUpdateWrapper<UserTimeline> wrapper = new LambdaUpdateWrapper<>();
		wrapper.eq(UserTimeline::getActorId, actorId)
				.eq(UserTimeline::getTargetId, targetId)
				.eq(UserTimeline::getTargetType, targetType);
		wrapper.set(UserTimeline::getIsValid, isValid);
		this.update(wrapper);
		logger.info("更新用户时间线成功，用户id：{}，事件id：{}，事件类型：{}，是否有效：{}", actorId, targetId, targetType, isValid);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int cleanupInvalidTimelines() {
		// 先统计无效记录数量
		long countToDelete = userTimelineMapper.countInvalidTimelines();
		if (countToDelete == 0) {
			logger.info("无需清理，没有无效的动态数据");
			return 0;
		}
		
		// 物理删除is_valid为0的记录
		int deletedCount = userTimelineMapper.cleanupInvalidTimelines();
		logger.info("清理无效动态数据完成，尝试删除 {} 条记录，实际删除 {} 条记录", countToDelete, deletedCount);
		return deletedCount;
	}
}