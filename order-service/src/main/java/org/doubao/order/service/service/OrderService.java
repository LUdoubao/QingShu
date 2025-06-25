package org.doubao.order.service.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.order.service.entity.Order;

public interface OrderService extends IService<Order> {
	void createOrder(Order order);

	void handlePaymentSuccess(Long orderId);

}
