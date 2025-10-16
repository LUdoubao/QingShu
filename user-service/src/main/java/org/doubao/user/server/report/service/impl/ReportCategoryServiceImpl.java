package org.doubao.user.server.report.service.impl;

import org.doubao.user.server.report.dto.response.CategoryTreeDTO;
import org.doubao.user.server.report.entity.ReportCategory;
import org.doubao.user.server.report.mapper.ReportCategoryMapper;
import org.doubao.user.server.report.service.ReportCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportCategoryServiceImpl implements ReportCategoryService {

	@Autowired
	private ReportCategoryMapper categoryMapper;

	/**
	 * 获取分类树（启用状态），并缓存结果
	 */
	@Override
	@Cacheable(value = "report:category", key = "'tree'")
	public List<CategoryTreeDTO> getCategoryTree() {
		// 1. 查询所有启用的分类
		List<ReportCategory> allCategories = categoryMapper.selectAllEnabled();
		if (allCategories.isEmpty()) {
			return new ArrayList<>();
		}

		// 2. 按父ID分组，便于构建树形结构
		Map<Integer, List<ReportCategory>> categoryMap = allCategories.stream()
				.collect(Collectors.groupingBy(ReportCategory::getParentId));

		// 3. 构建树形结构（从顶级分类开始，parentId=0）
		return buildCategoryTree(0, categoryMap);
	}

	/**
	 * 递归构建分类树
	 */
	private List<CategoryTreeDTO> buildCategoryTree(Integer parentId, Map<Integer, List<ReportCategory>> categoryMap) {
		List<ReportCategory> children = categoryMap.getOrDefault(parentId, new ArrayList<>());
		if (children.isEmpty()) {
			return null;
		}

		return children.stream().map(category -> {
			CategoryTreeDTO dto = new CategoryTreeDTO();
			BeanUtils.copyProperties(category, dto);

			// 递归构建子分类
			dto.setChildren(buildCategoryTree(category.getId(), categoryMap));
			return dto;
		}).collect(Collectors.toList());
	}

	@Override
	public CategoryTreeDTO getCategoryById(Long id) {
		// 实际实现：查询单个分类详情并返回
		// 可根据需要实现缓存逻辑
		return null;
	}
}