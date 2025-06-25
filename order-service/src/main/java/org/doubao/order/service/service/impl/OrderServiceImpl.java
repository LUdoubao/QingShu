package org.doubao.order.service.service.impl;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.PaymentStatus;
import org.doubao.mall.common.entity.Result;
import org.doubao.order.service.entity.Order;
import org.doubao.order.service.feign.PaymentFeignClient;
import org.doubao.order.service.feign.ProductFeignClient;
import org.doubao.order.service.mapper.OrderMapper;
import org.doubao.order.service.messaging.OrderEventPublisher;
import org.doubao.order.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
	@Autowired
	private ProductFeignClient productFeignClient;
	public static final String BUSINESS_EXCHANGE = "business.exchange";

	@Autowired
	private OrderEventPublisher orderEventPublisher;
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private PaymentFeignClient paymentFeignClient;
	@Override
	@Transactional
	public void createOrder(Order order) {
		Long orderId = System.currentTimeMillis();
		Long productId = order.getProductId();
		Integer quantity = order.getCount();
		try {
			// 1. 先扣减库存（远程调用）
			Double totalAmount = productFeignClient.decreaseStock(productId, quantity);

			order.setTotalAmount(totalAmount);
			order.setStatus(PaymentStatus.PENDING.getStatus());
			// 2. 本地创建订单
			this.save(order);

			// 3. 发布订单创建事件
			orderEventPublisher.publishOrderCreated(order);
		} catch (Exception e) {
			// ✅ 补偿库存
			productFeignClient.compensateStock(productId, quantity);
			throw new RuntimeException("下单失败，已执行库存补偿", e);
		}
	}

	@Override
	public void handlePaymentSuccess(Long orderId) {
		orderMapper.updateOrderStatus(orderId, PaymentStatus.PAID.getStatus());
	}
}