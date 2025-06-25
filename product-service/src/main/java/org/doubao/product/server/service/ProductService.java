package org.doubao.product.server.service;

import org.doubao.mall.common.entity.Product;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ProductService {
	List<Product> listAll();
	Product getById(Long id);
	void save(Product product);
	Double decreaseStock(Long productId, Integer amount);
	void compensateStock(Long productId, Integer amount);
}
