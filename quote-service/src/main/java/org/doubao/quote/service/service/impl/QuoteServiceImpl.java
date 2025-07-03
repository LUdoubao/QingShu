package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.entity.Category;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.entity.QuoteTag;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.mapper.QuoteMapper;
import org.doubao.quote.service.mapper.QuoteTagMapper;
import org.doubao.quote.service.service.CategoryService;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl extends ServiceImpl<QuoteMapper, Quote> implements QuoteService {

	@Resource
	private QuoteTagMapper quoteTagMapper;

	@Resource
	private CategoryService categoryService;

	@Override
	@SuppressWarnings("unchecked")
	public Result<Page<QuoteVo>> page(PageDto pageDto) {
		int page = pageDto.getPage();
		int size = pageDto.getSize();
		Long category = pageDto.getCategoryId();
		List<Long> tagIds = pageDto.getTagIds();

		if (page < 1 || size <= 0) {
			return Result.error("参数不合法");
		}

		LambdaQueryWrapper<Quote> queryWrapper = new LambdaQueryWrapper<Quote>()
				.eq(Quote::getDeleted, 0)
				.orderByDesc(Quote::getCreatedTime);

		if (category != null) {
			queryWrapper.eq(Quote::getCategoryId, category);
		}

		Page<Quote> pageData = this.page(new Page<>(page, size), queryWrapper);

		List<Quote> records = pageData.getRecords();

		Page<QuoteVo> pageVo = new Page<>(pageData.getCurrent(), pageData.getSize());

		if (!records.isEmpty()) {
			List<QuoteVo> quoteVoList = records.stream().map(quote -> {
				QuoteVo quoteVo = new QuoteVo();
				BeanUtils.copyProperties(quote, quoteVo);
				return quoteVo;
			}).collect(Collectors.toList());

			List<Long> quoteIds = quoteVoList.stream().map(QuoteVo::getId).collect(Collectors.toList());
			List<Long> categoryIds = quoteVoList.stream().map(QuoteVo::getCategoryId).collect(Collectors.toList());

			List<Map<String, Object>> tagMappings = quoteTagMapper.selectQuoteTagsWithDetails(quoteIds);
			Map<Long, String> categoryMap = new HashMap<>();
			if (!categoryIds.isEmpty()) {
				List<Category> categoryList = categoryService.list(new LambdaQueryWrapper<Category>().in(Category::getId, categoryIds));
				categoryMap = categoryList.stream().collect(Collectors.toMap(Category::getId, Category::getName));
			}

			// 构建 quoteId -> List<Tag>
			Map<Long, List<Tag>> quoteTagMap = new HashMap<>();
			List<Long> removeQuoteIds = new ArrayList<>();
			for (Map<String, Object> map : tagMappings) {
				Long quoteId = ((Number) map.get("quote_id")).longValue();
				Long tagId = ((Number) map.get("tag_id")).longValue();
				String tagName = (String) map.get("tag_name");

				if (tagIds != null && !tagIds.contains(tagId)) {
					removeQuoteIds.add(quoteId);
					continue;
				}

				Tag tag = new Tag();
				tag.setId(tagId);
				tag.setName(tagName);

				quoteTagMap.computeIfAbsent(quoteId, k -> new ArrayList<>()).add(tag);
			}

			// 移除quoteVoList，removeQuoteIds中的
			quoteVoList.removeIf(quoteVo -> removeQuoteIds.contains(quoteVo.getId()));
			// 设置 tags 字段
			for (QuoteVo quoteVo : quoteVoList) {
				quoteVo.setCategoryName(categoryMap.getOrDefault(quoteVo.getCategoryId(), "其他"));
				quoteVo.setTags(quoteTagMap.getOrDefault(quoteVo.getId(), new ArrayList<>()));
			}

			pageVo.setTotal(quoteVoList.size());
			pageVo.setRecords(quoteVoList);
		}

		return Result.success(pageVo);
	}

	@Override
	public Quote addQuote(QuoteDTO dto) {
		Quote q = new Quote();
		q.setContent(dto.getContent());
		q.setAuthor(dto.getAuthor());
		q.setSource(dto.getSource());
		q.setCategoryId(dto.getCategoryId());
		this.save(q);
		Long qId = q.getId();
		List<Long> tagIds = dto.getTagIds();
		if (tagIds != null && !tagIds.isEmpty()) {
			List<QuoteTag> quoteTags = new ArrayList<>();
			for (Long tagId : tagIds) {
				QuoteTag qt = new QuoteTag();
				qt.setQuoteId(qId);
				qt.setTagId(tagId);
				quoteTags.add(qt);
			}
			quoteTagMapper.insertBatch(quoteTags);
		}
		return q;
	}
}
