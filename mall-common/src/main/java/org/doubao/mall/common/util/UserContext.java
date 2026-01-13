package org.doubao.mall.common.util;


import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.vo.UserLoginVo;

public class UserContext {
	private static final ThreadLocal<UserLoginVo> currentUser = new ThreadLocal<>();

	public static void setUser(UserLoginVo user) {
		currentUser.set(user);
	}

	public static UserLoginVo getUser() {
		return currentUser.get();
	}

	public static void clear() {
		currentUser.remove();
	}

	public static Long getUserId() {
		// 获取当前用户ID
		UserLoginVo userInfo = UserContext.getUser();
		if (userInfo == null || userInfo.getId() == null) {
			throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
		}
		return userInfo.getId();
	}
}