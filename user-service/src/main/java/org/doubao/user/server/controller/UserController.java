package org.doubao.user.server.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.user.server.dto.*;
import org.doubao.user.server.service.UserService;
import org.doubao.user.server.vo.PageUserVo;
import org.doubao.user.server.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

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
	public Result<?> completeRegistration(@Valid @RequestBody VerifyCodeDto verifyDto) {
		userService.completeRegistration(
				new UserDto(verifyDto.getEmail(), "", ""),
				verifyDto.getCode()
		);
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

	@PutMapping("/me")
	public Result<?> updateProfile(
			@RequestHeader("X-User-Id") Long userId,
			@Valid @RequestBody UserUpdateDto dto
	) {
		userService.updateProfile(userId, dto);
		return Result.success(true);
	}

	@PatchMapping("/password")
	public Result<?> changePassword(
			@RequestHeader("X-User-Id") Long userId,
			@Valid @RequestBody PasswordChangeDto dto
	) {
		userService.changePassword(userId, dto);
		return Result.success(true);
	}

	// 管理员接口
	@GetMapping("/admin/users")
	public Result<PageUserVo<UserVo>> adminSearchUsers(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) String email,
			@RequestHeader("X-User-Role") String role
	) {
		if (!"ADMIN".equals(role)) {
			throw new BusinessException("权限不足", ErrorCode.FORBIDDEN);
		}
		return Result.success(userService.adminSearchUsers(page, size, status, email));
	}

	@PutMapping("/admin/users/{userId}/status")
	public Result<?> adminUpdateStatus(
			@PathVariable Long userId,
			@RequestParam Integer status,
			@RequestHeader("X-User-Role") String role
	) {
		if (!"ADMIN".equals(role)) {
			throw new BusinessException("权限不足", ErrorCode.FORBIDDEN);
		}
		userService.adminUpdateStatus(userId, status);
		return Result.success(true);
	}

	@DeleteMapping("/admin/users/{userId}")
	public Result<?> adminDeleteUser(
			@PathVariable Long userId,
			@RequestHeader("X-User-Role") String role
	) {
		if (!"ADMIN".equals(role)) {
			throw new BusinessException("权限不足", ErrorCode.FORBIDDEN);
		}
		userService.adminDeleteUser(userId);
		return Result.success(true);
	}

	@PutMapping("/admin/users/{userId}/role")
	public Result<?> adminUpdateRole(
			@PathVariable Long userId,
			@RequestParam String role,
			@RequestHeader("X-User-Role") String userRole
	) {
		if (!"ADMIN".equals(userRole)) {
			throw new BusinessException("权限不足", ErrorCode.FORBIDDEN);
		}
		userService.adminUpdateRole(userId, role);
		return Result.success(true);
	}
}