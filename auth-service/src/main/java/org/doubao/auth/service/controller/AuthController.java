package org.doubao.auth.service.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.doubao.auth.service.utils.JwtUtil;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("/login")
	public Result<UserInfo> login(@RequestBody UserInfo userInfo) {
		LOGGER.info("用户 {} 登录成功", userInfo.getUsername());
		String token = jwtUtil.generateToken(userInfo);
		userInfo.setToken(token);
		String key = Constants.REDIS_USER+ userInfo.getId();
		redisTemplate.opsForValue().set(key, userInfo);
		return Result.success(userInfo);
	}
	@GetMapping("/verify")
	public ResponseEntity<Map<String, Object>> verify(@RequestParam String token) {
		try {
			LOGGER.info("进入 /auth/verify，收到 token: {}", token);
			Claims claims = jwtUtil.getClaimsFromToken(token);
			return ResponseEntity.ok(claims);
		} catch (JwtException e) {
			LOGGER.error("token验证失败",e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("/token/expiration")
	public Result<Long> getTokenExpiration(@RequestBody String token) {
		try {
			Date expiration = jwtUtil.getExpirationDateFromToken(token);
			return Result.success(expiration.getTime());
		} catch (Exception e) {
			return Result.error("Token验证失败");
		}
	}
}
