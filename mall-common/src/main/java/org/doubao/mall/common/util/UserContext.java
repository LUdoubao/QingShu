package org.doubao.mall.common.util;


import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;

public class UserContext {
	private static final ThreadLocal<UserInfo> currentUser = new ThreadLocal<>();

	public static void setUser(UserInfo user) {
		currentUser.set(user);
	}

	public static UserInfo getUser() {
		return currentUser.get();
	}

	public static void clear() {
		currentUser.remove();
	}

	public static Long getUserId() {
		// 获取当前用户ID
		UserInfo userInfo = UserContext.getUser();
		if (userInfo == null || userInfo.getId() == null) {
			throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
		}
		return Long.valueOf(userInfo.getId());
	}
}