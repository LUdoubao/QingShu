package org.doubao.mall.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用返回结果封装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
	private Integer code;
	private String message;
	private T data;

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

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Result(int code, String success, T data) {
		this.code = code;
		this.message = success;
		this.data = data;
	}

	public static <T> Result<T> success(T data) {
		return new Result<>(200, "Success", data);
	}
	public static <T> Result<T> success() {
		return new Result<>(200, "Success", null);
	}

	public static <T> Result<T> error(String message) {
		return new Result<>(500, message, null);
	}

	public static <T> Result<T> error(int code, String message) {
		return new Result<>(code, message, null);
	}
}
