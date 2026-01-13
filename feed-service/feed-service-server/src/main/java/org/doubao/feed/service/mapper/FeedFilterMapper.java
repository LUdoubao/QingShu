package org.doubao.feed.service.mapper;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.feed.service.model.entity.FeedFilter;

@Mapper
public interface FeedFilterMapper extends BaseMapper<FeedFilter> {

	/**
	 * 根据用户ID查询过滤设置
	 */
	FeedFilter findByUserId(Long userId);
}