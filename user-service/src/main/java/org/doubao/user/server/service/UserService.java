package org.doubao.user.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.user.server.dto.*;
import org.doubao.user.server.entity.User;
import org.doubao.user.server.vo.PageUserVo;
import org.doubao.user.server.vo.UserVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

public interface UserService extends IService<User> {
	UserVo getById(Long id);
	void register(UserDto userDto);

	void completeRegistration(UserDto userDto);

	UserVo login(String username, String password);


	void updateProfile(UserUpdateDto dto);

	void changePassword(Long userId, PasswordChangeDto dto);

	PageUserVo<UserVo> adminSearchUsers(int page, int size, Integer status, String email);

	void adminUpdateStatus(Long userId, Integer status);

	void adminDeleteUser(Long userId);

	void adminUpdateRole(Long userId, String role);

	void loginOut(HttpServletRequest request);

	Boolean checkEmail(String email);

	Boolean sendEmailVerifyCode(String email);

	void updateEmail(UpdateEmailDto dto);

	String uploadAvatar(MultipartFile file, Long userId);

	List<UserInfo> usersByIds(Set<Long> userIds);
}