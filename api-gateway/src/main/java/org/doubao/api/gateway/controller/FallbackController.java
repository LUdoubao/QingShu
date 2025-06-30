package org.doubao.api.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	@GetMapping("/products/circuit")
	public ResponseEntity<Map<String, Object>> productCircuitFallback() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("code", 300);
		body.put("message", "商品服务暂时不可用，请稍后重试!");
		body.put("timestamp", System.currentTimeMillis());
		return ResponseEntity.ok(body);
	}
}