package org.doubao.order.service.feign;

import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment-service")
public interface PaymentFeignClient {

	@PostMapping("/payments/pay")
	Result<String> pay(@RequestParam("orderId") Long orderId, @RequestParam("amount") Double amount);

	@GetMapping("/payments/status/{orderId}")
	String getPaymentStatus(@PathVariable("orderId") Long orderId);
}