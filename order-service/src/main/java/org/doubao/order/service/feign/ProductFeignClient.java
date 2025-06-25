package org.doubao.order.service.feign;

import org.doubao.mall.common.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductFeignClient {
	@GetMapping("/products/{id}")
	Product getProductById(@PathVariable("id") Long id);

	@PostMapping("/products/decreaseStock")
	Double decreaseStock(@RequestParam("productId") Long productId, @RequestParam("amount") Integer amount);

	@PostMapping("/products/compensateStock")
	void compensateStock(@RequestParam("productId") Long productId, @RequestParam("amount") Integer amount);
}
