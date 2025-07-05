package org.doubao.quote.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.quote.service.entity.QuoteTag;
import org.doubao.quote.service.entity.QuoteVerify;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuoteVerifyMapper extends BaseMapper<QuoteVerify> {
}