package org.doubao.product.server.service.impl;

import org.doubao.mall.common.entity.Product;
import org.doubao.product.server.mapper.ProductMapper;
import org.doubao.product.server.service.ProductService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {
	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private RedissonClient redissonClient;
	@Override
	public List<Product> listAll() {
		return productMapper.selectList(null);
	}

	@Override
	public Product getById(Long id) {
		String key = "product:" + id;
		Product cached = (Product) redisTemplate.opsForValue().get(key);
		if (cached != null) return cached;
		Product db = productMapper.selectById(id);
		if (db != null) redisTemplate.opsForValue().set(key, db, 10, TimeUnit.MINUTES);
		return db;
	}

	@Override
	public void save(Product product) {
		productMapper.insert(product);
		redisTemplate.delete("product:" + product.getId());
	}

	@Override
	public Double decreaseStock(Long productId, Integer amount) {
		String lockKey = "lock:product:stock:" + productId;
		RLock lock = redissonClient.getLock(lockKey);
		boolean locked = false;
		double priceAll = 0.0;
		try {
			locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
			if (!locked) throw new RuntimeException("获取库存锁失败");

			Product product = productMapper.selectById(productId);
			if (product == null) throw new RuntimeException("商品不存在");

			if (product.getStock() < amount) throw new RuntimeException("库存不足");

			product.setStock(product.getStock() - amount);
			productMapper.updateById(product);

			priceAll = product.getPrice() * amount;
			// 更新缓存
			redisTemplate.opsForValue().set("product:" + productId, product, 10, TimeUnit.MINUTES);

		} catch (InterruptedException e) {
			throw new RuntimeException("库存锁异常", e);
		} finally {
			if (locked) lock.unlock();
		}
		return priceAll;
	}
	@Override
	public void compensateStock(Long productId, Integer amount) {
		String lockKey = "lock:product:stock:" + productId;
		RLock lock = redissonClient.getLock(lockKey);
		boolean locked = false;

		try {
			locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
			if (!locked) throw new RuntimeException("获取库存锁失败（补偿）");

			Product product = productMapper.selectById(productId);
			if (product == null) throw new RuntimeException("商品不存在（补偿）");

			product.setStock(product.getStock() + amount);
			productMapper.updateById(product);

			redisTemplate.opsForValue().set("product:" + productId, product, 10, TimeUnit.MINUTES);

		} catch (InterruptedException e) {
			throw new RuntimeException("补偿库存异常", e);
		} finally {
			if (locked) lock.unlock();
		}
	}
}