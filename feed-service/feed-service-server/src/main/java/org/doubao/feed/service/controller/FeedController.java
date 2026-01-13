package org.doubao.feed.service.controller;

import org.doubao.feed.service.model.dto.DynamicDTO;
import org.doubao.feed.service.model.dto.FilterSettingDTO;
import org.doubao.feed.service.model.dto.UpdateValidDto;
import org.doubao.feed.service.service.FeedAggregationService;
import org.doubao.feed.service.service.FeedFilterService;
import org.doubao.feed.service.service.UnreadService;
import org.doubao.feed.service.service.UserTimelineService;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/feed")
public class FeedController {

	private static final Logger log = LoggerFactory.getLogger(FeedController.class);
	@Resource
	private FeedAggregationService aggregationService;
	@Resource
	private UnreadService unreadService;
	@Resource
	private FeedFilterService filterService;
	@Resource
	private UserTimelineService userTimelineService;

	/**
	 * 更新动态流状态
	 */
	@PostMapping("/update-status")
	public Result<Void> updateFeedStatus(
			@RequestBody UpdateValidDto updateValidDto) {
		userTimelineService.updateValid(updateValidDto);
		return Result.success();
	}

	/**
	 * 获取动态流列表
	 */
	@GetMapping("/list")
	public Result<PageResult<DynamicDTO>> getFeedList(
			@RequestParam(required = false) Long targetUserId,

			@RequestParam(defaultValue = "1") int page,

			@RequestParam(defaultValue = "10") int pageSize,

			@RequestParam(defaultValue = "0") int sortType) {
		Long userId = UserContext.getUserId();
		try {
			// 参数校验
			if (page < 1 || pageSize < 1 || pageSize > 50) {
				return Result.error("参数错误，pageNum≥1，1≤pageSize≤50");
			}

			// 获取动态列表
			PageResult<DynamicDTO> feedPage = aggregationService.aggregateUserFeeds(userId, targetUserId, page, pageSize, sortType);

			// 应用过滤规则
			// List<DynamicDTO> filtered = filterService.applyFilter(userId, feedPage.getList());
			// feedPage.setList(filtered);

			// 如果是第一页且有数据，更新最后阅读时间
			if (page == 1 && !feedPage.getList().isEmpty()) {
				LocalDateTime lastTime = feedPage.getList().get(0).getEventTime();
				unreadService.updateLastReadTime(userId, lastTime);
			}

			return Result.success(feedPage);
		} catch (Exception e) {
			log.error("[getFeedList] 获取动态流失败，userId: {}", userId);
			log.error("[getFeedList] 获取动态流失败，失败信息", e);
			return Result.error("获取动态流失败");
		}
	}

	/**
	 * 获取未读动态数量
	 */
	@GetMapping("/unread/count")
	public Result<Long> getUnreadCount(
			@RequestHeader("X-User-Id") Long userId) {

		try {
			long count = unreadService.getUnreadCount(userId);
			return Result.success(count);
		} catch (Exception e) {
			log.error("[getUnreadCount] 获取未读数量失败，userId: {}", userId, e);
			return Result.error("获取未读数量失败");
		}
	}

	/**
	 * 标记所有动态为已读
	 */
	@PostMapping("/mark-all-read")
	public Result<Boolean> markAllAsRead(
			@RequestHeader("X-User-Id") Long userId) {

		try {
			unreadService.markAllAsRead(userId);
			return Result.success(true);
		} catch (Exception e) {
			log.error("[markAllAsRead] 标记已读失败，userId: {}", userId, e);
			return Result.error("标记已读失败");
		}
	}

	/**
	 * 设置动态过滤规则
	 */
	@PostMapping("/filter/set")
	public Result<Boolean> setFilterRules(
			@RequestHeader("X-User-Id") Long userId,

			@Validated @RequestBody FilterSettingDTO settingDTO) {

		try {
			filterService.saveFilterSetting(userId, settingDTO);
			return Result.success(true);
		} catch (Exception e) {
			log.error("[setFilterRules] 设置过滤规则失败，userId: {}", userId, e);
			return Result.error("设置过滤规则失败");
		}
	}

	/**
	 * 获取当前过滤规则
	 */
	@GetMapping("/filter/get")
	public Result<FilterSettingDTO> getFilterRules(
			@RequestHeader("X-User-Id") Long userId) {

		try {
			FilterSettingDTO settingDTO = filterService.getFilterSetting(userId);
			return Result.success(settingDTO);
		} catch (Exception e) {
			log.error("[getFilterRules] 获取过滤规则失败，userId: {}", userId, e);
			return Result.error("获取过滤规则失败");
		}
	}
}