package org.doubao.user.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.user.server.dto.PasswordChangeDto;
import org.doubao.user.server.dto.UserDto;
import org.doubao.user.server.dto.UserUpdateDto;
import org.doubao.user.server.entity.User;
import org.doubao.user.server.vo.PageUserVo;
import org.doubao.user.server.vo.UserVo;

import java.util.List;

public interface UserService extends IService<User> {
	UserVo getById(Long id);
	void register(UserDto userDto);

	void completeRegistration(UserDto userDto, String code);

	UserVo login(String username, String password);


	void updateProfile(Long userId, UserUpdateDto dto);

	void changePassword(Long userId, PasswordChangeDto dto);

	PageUserVo<UserVo> adminSearchUsers(int page, int size, Integer status, String email);

	void adminUpdateStatus(Long userId, Integer status);

	void adminDeleteUser(Long userId);

	void adminUpdateRole(Long userId, String role);
}