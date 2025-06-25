package org.doubao.order.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.order.service.entity.Order;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
	void updateOrderStatus(@Param("id") Long id, @Param("status") String status);
}