package org.doubao.user.server.core.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.user.server.core.dto.*;
import org.doubao.user.server.core.service.UserService;
import org.doubao.user.server.core.vo.UserVo;
import org.doubao.user.server.log.annotation.LogRecord;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/user")
public class UserController {
	@Resource
	private UserService userService;

	@LogRecord(operationType = "REGISTER", description = "用户注册")
	@PostMapping("/register")
	public Result<?> startRegistration(@Valid @RequestBody UserDto userDto) {
		userService.register(userDto);
		return Result.success(true);
	}

	@PostMapping("/forgot-password/code")
	public Result<?> forgotPasswordCode(@RequestBody ForgotPasswordCodeDto forgotPasswordCodeDto) {
		userService.forgotPasswordCode(forgotPasswordCodeDto);
		return Result.success(true);
	}
	@PostMapping("/forgot-password/verify")
	public Result<?> forgotPasswordVerify(@RequestBody ForgotPasswordVerifyDto forgotPasswordVerifyDto) {
		userService.forgotPasswordVerify(forgotPasswordVerifyDto);
		return Result.success(true);
	}
	@LogRecord(operationType = "RESET_PASSWORD", description = "重置密码")
	@PostMapping("/forgot-password/reset")
	public Result<?> forgotPasswordReset(@RequestBody ForgotPasswordResetDto forgotPasswordResetDto) {
		userService.forgotPasswordReset(forgotPasswordResetDto);
		return Result.success(true);
	}

	@PostMapping("/verify")
	public Result<?> completeRegistration(@Valid @RequestBody UserDto userDto) {
		userService.completeRegistration(userDto);
		return Result.success(true);
	}

	@LogRecord(operationType = "LOGIN", description = "用户登录")
	@PostMapping("/login")
	public Result<UserLoginVo> login(@RequestBody LoginDto loginDto) {
		return Result.success(userService.login(loginDto.getUsername(), loginDto.getPassword()));
	}

	@GetMapping("/get/{userId}")
	public Result<UserInfoProfile> getUser(@PathVariable Long userId) {
		return Result.success(userService.getById(userId));
	}

	@GetMapping("/editGet")
	public Result<UserInfoProfileEdit> editGet() {
		return Result.success(userService.editGet());
	}
	@GetMapping("/getWithSignature/{userId}")
	public Result<UserInfoProfileEdit> getWithSignature(@PathVariable Long userId) {
		return Result.success(userService.getWithSignature(userId));
	}
	@GetMapping("/editGetEmail")
	public Result<String> editGetEmail() {
		return Result.success(userService.editGetEmail());
	}

	@PostMapping("/listByIds")
	public Result<List<UserInfoDes>> listByIds(@RequestBody Set<Long> userIds) {
		return Result.success(userService.usersByIds(userIds));
	}


	@LogRecord(operationType = "UPDATE_USER_INFO", description = "更新用户信息")
	@PostMapping("/updateInfo")
	public Result<?> updateProfile(
			@Valid @RequestBody UserUpdateDto dto
	) {
		userService.updateProfile(dto);
		return Result.success(true);
	}

	@PostMapping("/password")
	public Result<?> changePassword(
			@Valid @RequestBody PasswordChangeDto dto
	) {
		userService.changePassword(dto);
		return Result.success(true);
	}


	@PostMapping("/logout")
	public Result<?> logout(HttpServletRequest request) {
		userService.loginOut(request);
		return Result.success(true);
	}

	@PostMapping("/check/email")
	public Result<?> checkEmail(@RequestParam String email) {
		return Result.success(userService.checkEmail(email));
	}

	@PostMapping("/send-email-verify-code")
	public Result<?> sendEmailVerifyCode(@RequestParam String email) {
		return Result.success(userService.sendEmailVerifyCode(email));
	}

	@PostMapping("/update-email")
	public Result<?> updateEmail(@RequestBody UpdateEmailDto dto) {
		userService.updateEmail(dto);
		return Result.success(true);
	}

	@PostMapping("/upload/avatar")
	public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam Long userId) {
		return Result.success(userService.uploadAvatar(file, userId));
	}

	@PostMapping("/upload/bg")
	public Result<?> uploadBg(@RequestParam("file") MultipartFile file, @RequestParam Long userId) {
		return Result.success(userService.uploadBg(file, userId));
	}

	@PostMapping("/inner/exists")
	boolean checkUserExists(@RequestBody Map<String, String> request) {
		return userService.checkUserExists(request);
	}
}