package org.doubao.quote.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.quote.service.entity.Category;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
