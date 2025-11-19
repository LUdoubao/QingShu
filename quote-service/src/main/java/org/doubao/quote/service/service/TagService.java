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

	/**
	 * 条件查询标签列表
	 */
	Result<List<TagCountVo>> listQuery(TagQuery tagQuery);

	/**
	 * 新增标签
	 * 创建新的标签记录，会进行重复性校验
	 *
	 * @param tag 标签实体对象，包含标签名称
	 * @return 包含新增标签信息的结果封装对象
	 */
	Result<Tag> add(Tag tag);

	/**
	 * 根据引用次数获取热门标签列表
	 * 按照标签被引用的次数降序排列，用于展示热门标签
	 *
	 * @param keyword 搜索关键词，可空，用于按名称模糊筛选标签
	 * @param limit 返回记录条数限制
	 * @return 热门标签列表，按引用次数降序排列
	 */
	List<Tag> listTopTagsByQuoteCount(String keyword, int limit);

	/**
	 * 根据标签名称查询标签ID列表
	 * 主要用于通过标签名称快速查找对应的标签标识
	 *
	 * @param keyword 标签名称关键词，支持模糊匹配
	 * @return 匹配的标签ID列表
	 */
	List<Long> listTagIdsByName(String keyword);
}
