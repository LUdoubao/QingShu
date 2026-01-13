package org.doubao.auth.service.controller;

import org.doubao.auth.service.service.AuthService;
import org.doubao.auth.service.utils.JwtUtil;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.UserLoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private JwtUtil jwtUtil;
	@Resource
	private AuthService authService;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("/login")
	public Result<UserLoginVo> login(@RequestBody UserLoginVo userLoginVo) {
		return authService.login(userLoginVo);
	}
	@GetMapping("/verify")
	public ResponseEntity<Map<String, Object>> verify(@RequestParam String token) {
		return  authService.verify(token);
	}
	@GetMapping("/token/webSocket")
	public Result<String> webSocket(@RequestParam String token) {
		return authService.webSocket(token);
	}

	@PostMapping("/token/expiration")
	public Result<Long> getTokenExpiration(@RequestBody String token) {
		return authService.getTokenExpiration(token);
	}
}