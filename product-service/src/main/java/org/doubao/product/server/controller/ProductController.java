package org.doubao.product.server.controller;

import org.doubao.mall.common.entity.Product;
import org.doubao.product.server.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@GetMapping("/list")
	public List<Product> list() {
		return productService.listAll();
	}

	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Long id) {
		return productService.getById(id);
	}

	@PostMapping
	public void save(@RequestBody Product product) {
		productService.save(product);
	}

	@PostMapping("/decreaseStock")
	public Double decreaseStock(@RequestParam("productId") Long productId, @RequestParam("amount") Integer amount) {
		return productService.decreaseStock(productId, amount);
	}

	@PostMapping("/compensateStock")
	public void compensateStock(@RequestParam("productId") Long productId,
								@RequestParam("amount") Integer amount) {
		productService.compensateStock(productId, amount);
	}
}
