package org.doubao.dialog.service.feign;

import org.doubao.dialog.service.req.NotificationReq;
import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "notification-service", path = "/notification")
public interface NotificationFeignClient {
	@PostMapping("/send")
	Result<Void> sendNotification(NotificationReq notificationReq);
}
