package org.doubao.payment.service.service.impl;

import org.doubao.payment.service.dto.PaymentRecordDto;
import org.doubao.payment.service.entity.PaymentRecord;
import org.doubao.payment.service.feign.OrderFeignClient;
import org.doubao.payment.service.mapper.PaymentRecordMapper;
import org.doubao.payment.service.service.PaymentService;
import org.doubao.payment.service.stream.PaymentEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	private PaymentRecordMapper paymentRecordMapper;

	@Autowired
	private PaymentEventPublisher paymentEventPublisher;

	@Autowired
	private OrderFeignClient orderFeignClient;

	@Override
	@Transactional
	public void pay(PaymentRecordDto paymentRecord) {
		// 创建支付记录（模拟）
		PaymentRecord record = new PaymentRecord();
		Long orderId = Long.valueOf(paymentRecord.getOrderId());
		record.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
		record.setOrderId(orderId);
		record.setAmount(paymentRecord.getAmount());
		record.setPaymentMethod(paymentRecord.getPaymentMethod());
		record.setStatus("SUCCESS");
		record.setCreateTime(LocalDateTime.now());
		record.setUpdateTime(LocalDateTime.now());

		paymentRecordMapper.insert(record);

		onPaymentSuccess(orderId);

		// 模拟支付成功后通知订单服务
		paymentEventPublisher.publishPaymentSuccess(orderId);
	}

	// 支付成功后调用
	public void onPaymentSuccess(Long orderId) {
		orderFeignClient.notifyPaymentSuccess(orderId);
	}
}