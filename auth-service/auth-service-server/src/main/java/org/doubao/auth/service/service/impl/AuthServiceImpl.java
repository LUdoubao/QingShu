package org.doubao.auth.service.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.doubao.auth.service.service.AuthService;
import org.doubao.auth.service.utils.JwtUtil;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.UserLoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
	@Autowired
	private JwtUtil jwtUtil;
	@Override
	public Result<UserLoginVo> login(UserLoginVo userLoginVo) {
		String token = jwtUtil.generateToken(userLoginVo);
		userLoginVo.setToken(token);
		return Result.success(userLoginVo);
	}

	@Override
	public Result<Long> getTokenExpiration(String token) {
		try {
			Date expiration = jwtUtil.getExpirationDateFromToken(token);
			return Result.success(expiration.getTime());
		} catch (Exception e) {
			return Result.error("Token验证失败");
		}
	}

	@Override
	public Result<String> webSocket(String token) {
		try {
			LOGGER.info("进入 /auth/webSocket，收到 token: {}", token);
			Claims claims = jwtUtil.getClaimsFromToken(token);
			if (claims != null) {
				Long userId = claims.get("userId", Long.class);
				LOGGER.info("用户 {} webSocket验证成功", userId);
				return Result.success(String.valueOf(userId));
			}
			return Result.error("Token验证失败");
		} catch (JwtException e) {
			LOGGER.error("token验证失败",e);
			return Result.error("Token验证失败");
		}
	}

	@Override
	public ResponseEntity<Map<String, Object>> verify(String token) {
		try {
			Claims claims = jwtUtil.getClaimsFromToken(token);
			return ResponseEntity.ok(claims);
		} catch (JwtException e) {
			LOGGER.error("token验证失败",e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
}