package org.doubao.mall.common.util;


import org.doubao.mall.common.entity.UserInfo;

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


}