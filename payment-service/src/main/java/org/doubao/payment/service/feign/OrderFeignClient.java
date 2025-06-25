package org.doubao.payment.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderFeignClient {

	@PostMapping("/order/paySuccess")
	void notifyPaymentSuccess(@RequestParam("orderId") Long orderId);
}