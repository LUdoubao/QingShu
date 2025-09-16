package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.entity.Category;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.mapper.CategoryMapper;
import org.doubao.quote.service.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

	@Override
	public Result<List<Category>> listByCategoryName(String categoryName) {
		return Result.success(this.list(new LambdaQueryWrapper<Category>().like(Category::getName, categoryName == null ? "" : categoryName)));
	}


}
