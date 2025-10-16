package org.doubao.user.server.report.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 举报分类树响应DTO
 */
public class CategoryTreeDTO {

	@ApiModelProperty("分类ID")
	private Long id;

	@ApiModelProperty("分类名称")
	private String name;

	@ApiModelProperty("层级：1=一级，2=二级，3=三级")
	private Integer level;

	@ApiModelProperty("子分类列表")
	private List<CategoryTreeDTO> children;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public List<CategoryTreeDTO> getChildren() {
		return children;
	}

	public void setChildren(List<CategoryTreeDTO> children) {
		this.children = children;
	}
}