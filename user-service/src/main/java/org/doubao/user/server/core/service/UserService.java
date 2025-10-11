package org.doubao.user.server.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.server.core.dto.*;
import org.doubao.user.server.core.entity.User;
import org.doubao.user.server.core.vo.PageUserVo;
import org.doubao.user.server.core.vo.UserVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService extends IService<User> {
	UserInfoProfile getById(Long id);
	void register(UserDto userDto);

	void completeRegistration(UserDto userDto);

	UserLoginVo login(String username, String password);


	void updateProfile(UserUpdateDto dto);

	void changePassword(PasswordChangeDto dto);

	void loginOut(HttpServletRequest request);

	Boolean checkEmail(String email);

	Boolean sendEmailVerifyCode(String email);

	void updateEmail(UpdateEmailDto dto);

	String uploadAvatar(MultipartFile file, Long userId);

	String uploadBg(MultipartFile file, Long userId);

	List<UserInfoDes> usersByIds(Set<Long> userIds);

	boolean checkUserExists(Map<String, String> request);

	UserInfoProfileEdit editGet();

	String editGetEmail();

	UserInfoProfileEdit getWithSignature(Long userId);

	void forgotPasswordCode(ForgotPasswordCodeDto forgotPasswordCodeDto);

	void forgotPasswordVerify(ForgotPasswordVerifyDto forgotPasswordVerifyDto);

	void forgotPasswordReset(ForgotPasswordResetDto forgotPasswordResetDto);
}