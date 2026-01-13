package org.doubao.user.service.utils;

import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.user.service.mapper.core.UserMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户状态校验工具
 */
@Component
public class UserValidator {

	@Resource
	private UserMapper userMapper; // 假设存在用户表Mapper

	/**
	 * 校验用户是否存在且活跃
	 */
	public void validateActiveUser(Long userId) {
		if (userId == null) {
			throw new BusinessException(ErrorCode.USER_ID_EMPTY);
		}
		Integer count = userMapper.existsByIdAndStatus(userId, 0);
		if (count == null || count == 0) {
			throw new BusinessException(ErrorCode.USER_DISABLED_OR_NOT_EXISTS);
		}
	}
}