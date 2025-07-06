package org.doubao.notification.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.doubao.mall.common.entity.Result;
import org.doubao.notification.service.dto.NotificationDTO;
import org.doubao.notification.service.dto.UnreadCountDTO;
import org.doubao.notification.service.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	@Resource
	private NotificationService notificationService;

	@GetMapping("/user/{userId}")
	public Result<Page<NotificationDTO>> getUserNotifications(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String status) {

		Page<NotificationDTO> result = notificationService.getUserNotifications(
				userId, page, size, status);

		return Result.success(result);
	}

	@GetMapping("/latest/{userId}")
	public Result<Page<NotificationDTO>> getLatestUserNotifications(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "1", required = false) int page,
			@RequestParam(defaultValue = "5", required = false) int size) {

		Page<NotificationDTO> result = notificationService.getUserNotifications(
				userId, page, size, null);

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

	@GetMapping("/detail/{id}")
	public Result<NotificationDTO> getNotificationDetail(@PathVariable Long id) {
		return Result.success(notificationService.getNotificationDetail(id));
	}
}