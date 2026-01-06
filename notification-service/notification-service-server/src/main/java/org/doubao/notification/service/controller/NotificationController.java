package org.doubao.notification.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.doubao.mall.common.entity.Result;
import org.doubao.notification.service.dto.NotificationDTO;
import org.doubao.notification.service.dto.NotificationQueryDto;
import org.doubao.notification.service.dto.UnreadCountDTO;
import org.doubao.notification.service.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/notifications")

public class NotificationController {

	@Resource
	private NotificationService notificationService;

	@PostMapping("/user")
	public Result<Page<NotificationDTO>> getUserNotifications(
			@RequestBody NotificationQueryDto notificationQuery) {

		Page<NotificationDTO> result = notificationService.getUserNotifications(
				notificationQuery);

		return Result.success(result);
	}

	@PostMapping("/latest")
	public Result<Page<NotificationDTO>> getLatestUserNotifications(
			@RequestBody NotificationQueryDto notificationQuery) {

		Page<NotificationDTO> result = notificationService.getUserNotifications(
				notificationQuery);

		return Result.success(result);
	}

	@GetMapping("/{id}/read")
	public Result<UnreadCountDTO> markAsRead(@PathVariable Long id) {
		return Result.success(notificationService.markAsRead(id));
	}

	@PostMapping("/batch-read")
	public Result<Boolean> batchMarkAsRead(@RequestBody List<Long> ids) {
		notificationService.batchMarkAsRead(ids);
		return Result.success(true);
	}

	@GetMapping("/{id}")
	public Result<Boolean> deleteNotification(@PathVariable Long id) {
		notificationService.deleteNotification(id);
		return Result.success(true);
	}

	@GetMapping("/unread-count/{userId}")
	public Result<UnreadCountDTO> getUnreadCount(@PathVariable Long userId) {
		return Result.success(notificationService.getUnreadCount(userId));
	}

	@PostMapping("/unread-count-type")
	public Result<UnreadCountDTO> getUnreadCountType(@RequestBody NotificationQueryDto notificationQueryDto) {
		return Result.success(notificationService.getUnreadCountType(notificationQueryDto));
	}

	@GetMapping("/detail/{id}")
	public Result<NotificationDTO> getNotificationDetail(@PathVariable Long id) {
		return Result.success(notificationService.getNotificationDetail(id));
	}
}