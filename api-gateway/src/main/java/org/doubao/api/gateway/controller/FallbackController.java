package org.doubao.api.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

	@GetMapping("/products/circuit")
	public ResponseEntity<String> productCircuitFallback() {
		return ResponseEntity.ok("商品服务暂时不可用，请稍后重试。");
	}

	@GetMapping("/products/rateLimit")
	public ResponseEntity<String> productRateLimitFallback() {
		return ResponseEntity.status(429).body("访问太频繁，请稍后再试！");
	}
}