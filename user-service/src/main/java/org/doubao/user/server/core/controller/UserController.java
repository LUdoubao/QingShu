package org.doubao.user.server.core.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.user.server.core.dto.*;
import org.doubao.user.server.core.service.UserService;
import org.doubao.user.server.core.vo.UserVo;
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

	@PostMapping("/register")
	public Result<?> startRegistration(@Valid @RequestBody UserDto userDto) {
		userService.register(userDto);
		return Result.success(true);
	}

	@PostMapping("/verify")
	public Result<?> completeRegistration(@Valid @RequestBody UserDto userDto) {
		userService.completeRegistration(userDto);
		return Result.success(true);
	}

	@PostMapping("/login")
	public Result<UserVo> login(@RequestBody LoginDto loginDto) {
		return Result.success(userService.login(loginDto.getUsername(), loginDto.getPassword()));
	}

	@GetMapping("/get/{userId}")
	public Result<UserVo> getUser(@PathVariable Long userId) {
		return Result.success(userService.getById(userId));
	}

	@PostMapping("/listByIds")
	public Result<List<UserInfo>> listByIds(@RequestBody Set<Long> userIds) {
		return Result.success(userService.usersByIds(userIds));
	}


	@PutMapping("/updateInfo")
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