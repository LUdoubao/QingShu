package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.ResultCode;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.util.UserContext;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Category;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.entity.QuoteTag;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.mapper.QuoteMapper;
import org.doubao.quote.service.mapper.QuoteTagMapper;
import org.doubao.quote.service.messaging.QuoteEventPublisher;
import org.doubao.quote.service.service.CategoryService;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.util.QuoteUtil;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl extends ServiceImpl<QuoteMapper, Quote> implements QuoteService {

	@Resource
	private QuoteEventPublisher quoteEventPublisher;
	@Resource
	private QuoteTagMapper quoteTagMapper;

	@Resource
	private CategoryService categoryService;

	@Override
	@SuppressWarnings("unchecked")
	public Result<Page<QuoteVo>> page(PageDto pageDto) {

		LambdaQueryWrapper<Quote> queryWrapper = buildQueryWrapper(pageDto);
		if (queryWrapper == null) {
			return Result.error("参数不合法");
		}

		return query(pageDto, queryWrapper);
	}

	@Override
	public Result<Quote> addQuote(QuoteDTO dto) {
		Quote q = new Quote();
		q.setContent(dto.getContent());
		q.setAuthor(dto.getAuthor());
		q.setSource(dto.getSource());
		q.setCategoryId(dto.getCategoryId());
		q.setStatus(1);
		this.save(q);
		Long qId = q.getId();
		List<Long> tagIds = dto.getTagIds();
		List<QuoteTag> quoteTags = new ArrayList<>();
		if (tagIds != null && !tagIds.isEmpty()) {
			for (Long tagId : tagIds) {
				QuoteTag qt = new QuoteTag();
				qt.setQuoteId(qId);
				qt.setTagId(tagId);
				quoteTags.add(qt);
			}
			quoteTagMapper.insertBatch(quoteTags);
		}
		quoteEventPublisher.publishQuoteAdd(q, quoteTags);

		return Result.success(q);
	}

	@Override
	public Result<String> deleteQuote(List<Long> quoteIds) {
		if (quoteIds == null || quoteIds.isEmpty()) {
			return Result.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage());
		}
		this.removeByIds(quoteIds);
		quoteTagMapper.deleteBatchIds(quoteIds);
		return Result.success(ResultCode.SUCCESS.getMessage());
	}

	@Override
	public Result<String> updateQuote(QuoteUpdateDto dto) {
		// 判断是否为管理员TODO
		//更新引文状态为待审核
		Long quoteId = dto.getQuoteId();
		LambdaUpdateWrapper<Quote> updateWrapper = new LambdaUpdateWrapper<Quote>();
		updateWrapper.eq(Quote::getId, quoteId);
		updateWrapper.set(Quote::getStatus, 0);
		this.update(updateWrapper);
		//推送审核信息到邮箱
		quoteEventPublisher.publishQuoteVerify(dto);
		return Result.success(ResultCode.SUCCESS.getMessage());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Result<QuoteVo> getDetailById(Long id) {
		LambdaQueryWrapper<Quote> queryWrapper = new LambdaQueryWrapper<Quote>()
				.eq(Quote::getDeleted, 0)
				.eq(Quote::getId, id)
				.eq(Quote::getStatus,1)
				.orderByDesc(Quote::getCreatedTime);
		Quote quote = this.getOne(queryWrapper);
		QuoteVo quoteVo = new QuoteVo();
		if (quote != null) {
			BeanUtils.copyProperties(quote, quoteVo);
			List<Map<String, Object>> tagMappings = quoteTagMapper.selectQuoteTagsWithDetails(Collections.singletonList(id));
			Long categoryId = quoteVo.getCategoryId();
			Category category = categoryService.getById(categoryId);
			Map<Long, List<Tag>> quoteTagMap = new HashMap<>();
			for (Map<String, Object> map : tagMappings) {
				Long quoteId = ((Number) map.get("quote_id")).longValue();
				Long tagId = ((Number) map.get("tag_id")).longValue();
				String tagName = (String) map.get("tag_name");

				Tag tag = new Tag();
				tag.setId(tagId);
				tag.setName(tagName);

				quoteTagMap.computeIfAbsent(quoteId, k -> new ArrayList<>()).add(tag);
			}
			quoteVo.setCategoryName(category.getName());
			quoteVo.setTags(quoteTagMap.getOrDefault(quoteVo.getId(), new ArrayList<>()));
		}
		return Result.success(quoteVo);
	}

	@Override
	public Result<Page<QuoteVo>> pageManager(PageDto pageDto) {
		LambdaQueryWrapper<Quote> queryWrapper = buildQueryWrapper(pageDto);
		if (queryWrapper == null) {
			return Result.error("参数不合法");
		}
		String userId = UserContext.getUser().getUserId();
		if (!"1".equals(userId)) {
			queryWrapper.eq(Quote::getCreatedId,UserContext.getUser().getUserId());
		}
		return query(pageDto, queryWrapper);
	}

	@SuppressWarnings("unchecked")
	private LambdaQueryWrapper<Quote> buildQueryWrapper(PageDto pageDto) {
		int page = pageDto.getPage();
		int size = pageDto.getSize();
		Long category = pageDto.getCategoryId();
		List<Long> tagIds = pageDto.getTagIds();

		if (page < 1 || size <= 0) {
			return null;
		}

		LambdaQueryWrapper<Quote> queryWrapper = new LambdaQueryWrapper<Quote>()
				.eq(Quote::getDeleted, 0)
				.eq(Quote::getStatus,1)
				.orderByDesc(Quote::getCreatedTime);

		if (category != null) {
			queryWrapper.eq(Quote::getCategoryId, category);
		}
		return queryWrapper;
	}

	private Result<Page<QuoteVo>> query(PageDto pageDto, LambdaQueryWrapper<Quote> queryWrapper) {
		int page = pageDto.getPage();
		int size = pageDto.getSize();
		List<Long> tagIds = pageDto.getTagIds();
		Page<Quote> pageData = this.page(new Page<>(page, size), queryWrapper);

		List<Quote> records = pageData.getRecords();

		Page<QuoteVo> pageVo = new Page<>(pageData.getCurrent(), pageData.getSize(), pageData.getTotal());

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

			pageVo.setRecords(quoteVoList);
		}
		return Result.success(pageVo);
	}

}
