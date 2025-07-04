package org.doubao.payment.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.payment.service.dto.PaymentRecordDto;
import org.doubao.payment.service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@PostMapping("/pay")
	public Result<String> pay(@RequestBody PaymentRecordDto paymentRecord){
		paymentService.pay(paymentRecord);
		return Result.success("SUCCESS");
	}

	@GetMapping("/status/{orderId}")
	public String getPaymentStatus(@PathVariable Long orderId) {
		// 查询支付状态逻辑
		return "SUCCESS";
	}

}