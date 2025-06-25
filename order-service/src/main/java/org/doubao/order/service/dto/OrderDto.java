package org.doubao.order.service.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.doubao.order.service.entity.Order;


public class OrderDto {
	private String id;
	private Long userId;
	private Long productId;
	private Integer count;
	private Double totalAmount;
	private String status;

	public static OrderDto toDto(Order order) {
		OrderDto orderDto = new OrderDto();
		orderDto.setId(order.getId().toString());
		orderDto.setUserId(order.getUserId());
		orderDto.setProductId(order.getProductId());
		orderDto.setCount(order.getCount());
		orderDto.setTotalAmount(order.getTotalAmount());
		orderDto.setStatus(order.getStatus());
		return orderDto;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}