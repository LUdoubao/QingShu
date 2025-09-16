package org.doubao.quote.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.TagCountVo;
import org.doubao.quote.service.dto.TagQuery;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.vo.QuoteVo;

import java.util.List;

public interface TagService extends IService<Tag> {
	Result<List<TagCountVo>> listQuery(TagQuery tagQuery);

	Result<Tag> add(Tag tag);

	List<Tag> listTopTagsByQuoteCount(String keyword, int limit);

	List<Long> listTagIdsByName(String keyword);
}
