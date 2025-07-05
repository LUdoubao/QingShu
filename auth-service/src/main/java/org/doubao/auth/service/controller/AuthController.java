package org.doubao.auth.service.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.doubao.auth.service.dto.LoginRequest;
import org.doubao.auth.service.dto.UserInfo;
import org.doubao.auth.service.entity.User;
import org.doubao.auth.service.service.UserService;
import org.doubao.auth.service.utils.JwtUtil;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private UserService userService;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("/login")
	public Result<UserInfo> login(@RequestBody LoginRequest request) {
		User user = userService.getByUsername(request.getUsername());
		if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			return Result.error("用户名或密码错误");
		}
		LOGGER.info("用户 {} 登录成功", user.getUsername());
		String token = jwtUtil.generateToken(user);
		UserInfo userInfo = new UserInfo();
		BeanUtils.copyProperties(user, userInfo);
		userInfo.setToken(token);
		return Result.success(userInfo);
	}
	@GetMapping("/verify")
	public ResponseEntity<Map<String, Object>> verify(@RequestParam String token) {
		try {
			LOGGER.info("进入 /auth/verify，收到 token: {}", token);
			Claims claims = jwtUtil.getUsernameFromToken(token);
			return ResponseEntity.ok(claims);
		} catch (JwtException e) {
			LOGGER.error("token验证失败",e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}
