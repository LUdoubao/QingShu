package org.doubao.quote.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
	/**
	 * 根据标签名称查询
	 * @param tagName 标签名称
	 * @return 标签列表
	 */
	Result<List<Category>> listByCategoryName(String tagName);

}
