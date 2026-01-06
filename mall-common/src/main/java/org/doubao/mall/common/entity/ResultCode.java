package org.doubao.mall.common.entity;


public enum ResultCode {
	SUCCESS(200, "操作成功"),
	FAIL(500, "操作失败"),
	UNAUTHORIZED(401, "未认证"),
	FORBIDDEN(403, "无权限"),
	NOT_FOUND(404, "接口不存在"),
	SERVER_ERROR(500, "服务器内部错误");

	private Integer code;
	private String message;

	ResultCode(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}