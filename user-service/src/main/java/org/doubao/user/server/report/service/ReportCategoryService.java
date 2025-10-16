package org.doubao.user.server.report.service;

import org.doubao.user.server.report.dto.response.CategoryTreeDTO;
import java.util.List;

/**
 * 举报分类服务接口
 */
public interface ReportCategoryService {

	/**
	 * 获取完整的举报分类树（启用状态）
	 */
	List<CategoryTreeDTO> getCategoryTree();

	/**
	 * 根据分类ID查询分类详情
	 */
	CategoryTreeDTO getCategoryById(Long id);
}