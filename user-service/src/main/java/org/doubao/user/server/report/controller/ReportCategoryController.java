package org.doubao.user.server.report.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.user.server.report.dto.response.CategoryTreeDTO;
import org.doubao.user.server.report.service.ReportCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 举报分类控制器
 */
@RestController
@RequestMapping("/user/report/category")
@Api(tags = "举报分类接口")
public class ReportCategoryController {

	@Autowired
	private ReportCategoryService categoryService;

	@GetMapping("/tree")
	@ApiOperation("获取举报分类树（所有启用的分类）")
	public List<CategoryTreeDTO> getCategoryTree() {
		return categoryService.getCategoryTree();
	}

	@GetMapping("/{id}")
	@ApiOperation("根据ID查询分类详情")
	public CategoryTreeDTO getCategoryById(Long id) {
		return categoryService.getCategoryById(id);
	}
}