package org.doubao.user.service.controller.report;



import org.doubao.user.service.dto.report.response.CategoryTreeDTO;
import org.doubao.user.service.service.report.ReportCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/user/report/category")
public class ReportCategoryController {

	@Autowired
	private ReportCategoryService categoryService;

	@GetMapping("/tree")
	public List<CategoryTreeDTO> getCategoryTree() {
		return categoryService.getCategoryTree();
	}

	@GetMapping("/{id}")
	//@ApiOperation("根据ID查询分类详情")
	public CategoryTreeDTO getCategoryById(Long id) {
		return categoryService.getCategoryById(id);
	}
}
