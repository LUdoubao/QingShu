package org.doubao.mall.common.entity;

public enum PaymentStatus {
	CANCELED("CANCELED", "取消"),
	COMPLETED("COMPLETED", "完成"),
	PAID("PAID", "支付成功"),
	PENDING("PENDING", "待支付"),
	SUCCESS("SUCCESS", "支付成功"),
	FAILED("FAILED", "支付失败");
	private String status;
	private String message;

	PaymentStatus(String status, String message) {
		this.status = status;
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public String getMessage() {
		return message;
	}
}
