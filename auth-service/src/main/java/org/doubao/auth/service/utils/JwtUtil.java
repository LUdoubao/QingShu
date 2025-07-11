package org.doubao.auth.service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.doubao.mall.common.entity.UserInfo;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

	public Claims getUsernameFromToken(String token) {
		return Jwts.parser()
				.setSigningKey(SECRET)
				.parseClaimsJws(token)
				.getBody();
	}
}
