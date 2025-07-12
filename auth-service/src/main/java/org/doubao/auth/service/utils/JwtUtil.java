package org.doubao.auth.service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.doubao.mall.common.entity.UserInfo;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
	private static final String SECRET = "doubao";

	public String generateToken(UserInfo user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", user.getUsername());
		claims.put("userId", user.getId());
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(user.getUsername())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1天
				.signWith(SignatureAlgorithm.HS256, SECRET)
				.compact();
	}

	public Claims getClaimsFromToken(String token) {
		return Jwts.parser()
				.setSigningKey(SECRET)
				.parseClaimsJws(token)
				.getBody();
	}

	// 从token中获取过期时间
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	// 从token中获取特定声明
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	// 获取token剩余有效时间（毫秒）
	public Long getRemainingMillis(String token) {
		try {
			Date expiration = getExpirationDateFromToken(token);
			return expiration.getTime() - System.currentTimeMillis();
		} catch (ExpiredJwtException e) {
			return 0L;
		}
	}
}
