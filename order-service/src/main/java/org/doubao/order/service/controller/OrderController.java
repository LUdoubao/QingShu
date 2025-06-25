package org.doubao.order.service.controller;

import org.doubao.mall.common.entity.PaymentStatus;
import org.doubao.order.service.dto.OrderDto;
import org.doubao.order.service.entity.Order;
import org.doubao.order.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping("/create")
	public String create(@RequestBody Order order) {
		orderService.createOrder(order);
		return "Order created";
	}

	@GetMapping("/list")
	public List<OrderDto> list() {
		return orderService.list().stream()
				.sorted(Comparator.comparing(order ->
						Objects.equals(order.getStatus(), PaymentStatus.PAID.getStatus())
				))
				.map(OrderDto::toDto)
				.collect(Collectors.toList());
	}

	@GetMapping("/detail/{orderId}")
	public OrderDto detail(@PathVariable Long orderId) {
		return OrderDto.toDto(orderService.getById(orderId));
	}


	@PostMapping("/paySuccess")
	public void notifyPaymentSuccess(@RequestParam("orderId") Long orderId) {
		orderService.handlePaymentSuccess(orderId);
	}
}