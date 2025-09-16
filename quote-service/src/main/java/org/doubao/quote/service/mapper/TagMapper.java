package org.doubao.quote.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.quote.service.dto.TagCountDTO;
import org.doubao.quote.service.dto.TagCountVo;
import org.doubao.quote.service.entity.Tag;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
	List<TagCountVo> selectTopTags(@Param("limit") int limit);

	List<TagCountDTO> selectTagWithQuoteCount(@Param("keyword") String keyword);

	List<Long> selectTagIdsByName(@Param("keyword") String keyword);
}
