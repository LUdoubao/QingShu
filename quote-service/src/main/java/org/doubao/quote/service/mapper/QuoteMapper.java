package org.doubao.quote.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.quote.service.entity.Quote;

@Mapper
public interface QuoteMapper extends BaseMapper<Quote> {
}
