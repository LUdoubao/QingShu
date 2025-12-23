package org.doubao.quote.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.entity.Category;
import org.doubao.quote.service.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@GetMapping("/list")
	public Result<List<Category>> list(@RequestParam(required = false) String categoryName) {
		return categoryService.listByCategoryName(categoryName);
	}

}

