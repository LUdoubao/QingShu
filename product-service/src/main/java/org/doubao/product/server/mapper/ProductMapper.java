package org.doubao.product.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.mall.common.entity.Product;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}