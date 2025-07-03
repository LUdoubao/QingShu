package org.doubao.quote.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
	Result<List<Category>> listByCategoryName(String tagName);
}
