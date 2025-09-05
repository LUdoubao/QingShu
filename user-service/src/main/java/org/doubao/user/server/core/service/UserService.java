package org.doubao.user.server.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.user.server.core.dto.PasswordChangeDto;
import org.doubao.user.server.core.dto.UpdateEmailDto;
import org.doubao.user.server.core.dto.UserDto;
import org.doubao.user.server.core.dto.UserUpdateDto;
import org.doubao.user.server.core.entity.User;
import org.doubao.user.server.core.vo.PageUserVo;
import org.doubao.user.server.core.vo.UserVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService extends IService<User> {
	UserVo getById(Long id);
	void register(UserDto userDto);

	void completeRegistration(UserDto userDto);

	UserVo login(String username, String password);


	void updateProfile(UserUpdateDto dto);

	void changePassword(PasswordChangeDto dto);

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

	boolean checkUserExists(Map<String, String> request);
}