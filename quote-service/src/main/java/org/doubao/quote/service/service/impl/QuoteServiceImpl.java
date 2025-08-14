package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.ResultCode;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.duplicate.check.CitationCheckService;
import org.doubao.quote.service.duplicate.check.DecisionEngine;
import org.doubao.quote.service.entity.*;
import org.doubao.quote.service.mapper.QuoteMapper;
import org.doubao.quote.service.mapper.QuoteTagMapper;
import org.doubao.quote.service.messaging.QuoteEventPublisher;
import org.doubao.quote.service.service.*;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl extends ServiceImpl<QuoteMapper, Quote> implements QuoteService {

	@Resource
	private QuoteEventPublisher quoteEventPublisher;
	@Resource
	private QuoteTagMapper quoteTagMapper;
	@Resource
	private QuoteTagService quoteTagService;
	@Resource
	private TagService tagService;
	@Resource
	private QuoteMapper quoteMapper;

	@Resource
	private QuoteVerifyService quoteVerifyService;
	@Resource
	private CategoryService categoryService;

	@Resource
	private CitationCheckService citationCheckService;
	@Override
	@SuppressWarnings("unchecked")
	public Result<Page<QuoteVo>> page(PageDto pageDto) {
		return query(pageDto);
	}

	@Override
	public Result<Quote> addQuote(QuoteDTO dto) {
		// 检查引文是否重复
		DecisionEngine.DuplicationResult duplicationResult = citationCheckService.checkCitation(dto.getContent(), dto.getAuthor(), dto.getSource(), dto.getOriginal() == 1);
		if (duplicationResult.getStatus() == DecisionEngine.DuplicationStatus.DUPLICATE) {
			throw new BusinessException(ErrorCode.CONTENT_EXISTS);
		}
		Quote q = new Quote();
		q.setContent(dto.getContent());
		q.setAuthor(dto.getAuthor());
		q.setSource(dto.getSource());
		q.setCategoryId(dto.getCategoryId());
		q.setOriginal(dto.getOriginal());
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
		UserInfo userInfo = UserContext.getUser();
		QuoteVo afterQuoteVo = dto.getAfterQuoteVo();
		Quote quote = new Quote();
		BeanUtils.copyProperties(afterQuoteVo, quote);
		List<Tag> tags = afterQuoteVo.getTags();
		if (userInfo.getId().equals("1") && userInfo.getUsername().equals("admin")) {
			// 管理员直接保存
			quote.setStatus(1);
			this.updateById(quote);
			List<QuoteTag> quoteTags = new ArrayList<>();
			if (tags != null && !tags.isEmpty()) {
				for (Tag tag : tags) {
					QuoteTag qt = new QuoteTag();
					qt.setQuoteId(afterQuoteVo.getId());
					qt.setTagId(tag.getId());
					quoteTags.add(qt);
				}
				saveOrUpdateQuoteTags(quoteTags);
			}
		} else {
			//非管理员更新引文状态为待审核
			Long quoteId = dto.getQuoteId();
			LambdaUpdateWrapper<Quote> updateWrapper = new LambdaUpdateWrapper<Quote>();
			updateWrapper.eq(Quote::getId, quoteId);
			updateWrapper.set(Quote::getStatus, 0);
			this.update(updateWrapper);
			// 同步到审核表
			QuoteVerify quoteVerify = new QuoteVerify();
			BeanUtils.copyProperties(quote,quoteVerify);
			quoteVerify.setUpdatedTime(LocalDateTime.now());
			if (tags != null && !tags.isEmpty()) {
				String tag = tags.stream().map(Tag::getId)
						.map(String::valueOf)
						.collect(Collectors.joining(","));
				quoteVerify.setTag(tag);
			}
			quoteVerifyService.save(quoteVerify);
			// 推送提交更新消息到用户消息中心
			quoteEventPublisher.pushQuoteUpdateNotification(Long.valueOf(userInfo.getId()),
					"QUOTE_UPDATED", "quote",
					quoteId, "SUCCESS", "提交修改成功");

			// 推送待审核消息到管理员消息中心
			quoteEventPublisher.pushQuoteUpdateNotification(1L,
					"QUOTE_VERIFY", "quote",
					quoteId, "SUCCESS", "待审核消息");
		}

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
				.eq(Quote::getStatus,1);
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
		String userId = UserContext.getUser().getId();
		if (!"1".equals(userId)) {
			pageDto.setUserId(Long.valueOf(userId));
		}
		return query(pageDto);
	}

	private Result<Page<QuoteVo>> query(PageDto pageDto) {
		int page = pageDto.getPage();
		int size = pageDto.getSize();
		Long categoryId = pageDto.getCategoryId();
		List<Long> tagIds = pageDto.getTagIds();
		Long userId = pageDto.getUserId();

		// 1. 查询总数
		long total = quoteMapper.countByTagIdsAndCategory(categoryId, tagIds, tagIds == null ? 0 : tagIds.size(), userId);

		// 2. 查询分页数据
		List<Quote> records = quoteMapper.selectByTagIdsAndCategory(
				categoryId,
				tagIds,
				tagIds == null ? 0 : tagIds.size(),
				size,
				(page - 1) * size,
				userId
		);

		Page<QuoteVo> pageVo = new Page<>(page, size, total);
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
			for (Map<String, Object> map : tagMappings) {
				Long quoteId = ((Number) map.get("quote_id")).longValue();
				Long tagId = ((Number) map.get("tag_id")).longValue();
				String tagName = (String) map.get("tag_name");

				Tag tag = new Tag();
				tag.setId(tagId);
				tag.setName(tagName);

				quoteTagMap.computeIfAbsent(quoteId, k -> new ArrayList<>()).add(tag);
			}

			// 移除quoteVoList，removeQuoteIds中的
			// 设置 tags 字段
			for (QuoteVo quoteVo : quoteVoList) {
				quoteVo.setCategoryName(categoryMap.getOrDefault(quoteVo.getCategoryId(), "其他"));
				quoteVo.setTags(quoteTagMap.getOrDefault(quoteVo.getId(), new ArrayList<>()));
			}

			pageVo.setRecords(quoteVoList);
		}
		return Result.success(pageVo);
	}

	@Override
	@Transactional
	public Result<String> verify(QuoteDTO dto) {
		int status = dto.getStatus();
		if (status != 0 && status != 1) {
			return Result.error("参数不合法");
		}

		Long quoteId = dto.getId();
		UserInfo user = UserContext.getUser();
		switch (status) {
			case 0:
				// 审核不通过
				// 引文表状态恢复1
				LambdaUpdateWrapper<Quote> updateWrapper = new LambdaUpdateWrapper<>();
				updateWrapper.eq(Quote::getId, quoteId)
						.set(Quote::getStatus, 1);
				this.update(updateWrapper);

				quoteEventPublisher.pushQuoteVerifyNotification(
						quoteId,
						dto.getContent(),
						"REJECTED",
						"审核驳回",
						user.getUsername(),
						dto.getCreatedId()
				);
				break;
			default:
				Quote quote = this.getById(quoteId);
				quote.setStatus(1);
				List<Long> tagIds = dto.getTagIds();
				List<QuoteTag> quoteTags = new ArrayList<>();
				if (tagIds != null && !tagIds.isEmpty()) {
					for (Long tagId : tagIds) {
						QuoteTag qt = new QuoteTag();
						qt.setQuoteId(quoteId);
						qt.setTagId(tagId);
						quoteTags.add(qt);
					}
					saveOrUpdateQuoteTags(quoteTags);
				}
				this.updateById(quote);
				quoteEventPublisher.pushQuoteVerifyNotification(
						quoteId,
						dto.getContent(),
						"APPROVED",
						"审核通过",
						user.getUsername(),
						dto.getCreatedId()
				);
				break;
		}
		// 删除审核表数据
		LambdaQueryWrapper<QuoteVerify> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(QuoteVerify::getId, quoteId);
		quoteVerifyService.remove(wrapper);
		// 推送消息到用户消息中心TODO
		return Result.success(ResultCode.SUCCESS.getMessage());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Result<QuoteVo> getVerifyDetailById(Long id) {
		LambdaQueryWrapper<QuoteVerify> queryWrapper = new LambdaQueryWrapper<QuoteVerify>()
				.eq(QuoteVerify::getId, id);
		QuoteVerify quoteVerify = quoteVerifyService.getOne(queryWrapper);
		QuoteVo quoteVo = new QuoteVo();
		if (quoteVerify != null) {
			BeanUtils.copyProperties(quoteVerify, quoteVo);
			String verifyTag = quoteVerify.getTag();
			List<Tag> tags = new ArrayList<>();
			if (StringUtils.isNotEmpty(verifyTag)) {
				String[] split = verifyTag.split(",");
				List<Long> tagIdList = Arrays.stream(split)
						.map(Long::valueOf)
						.collect(Collectors.toList());
				tags = tagService.listByIds(tagIdList);
			}
			Long categoryId = quoteVo.getCategoryId();
			Category category = categoryService.getById(categoryId);
			quoteVo.setCategoryName(category.getName());

			if (!tags.isEmpty()) {
				quoteVo.setTags(tags);
			}
		}
		return Result.success(quoteVo);
	}

	@Override
	public Result<Page<QuoteVo>> verifyPage(PageDto pageDto) {
		return queryVerify(pageDto);
	}

	@Override
	public Result<List<Map<String, Object>>> batch(List<Long> ids) {
		LambdaQueryWrapper<Quote> queryWrapper = new LambdaQueryWrapper<Quote>()
				.in(Quote::getId, ids);
		List<Quote> quotes = this.list(queryWrapper);

		List<Map<String, Object>> mapList = new ArrayList<>();
		if (!quotes.isEmpty()) {
			List<QuoteVo> quoteVoList = quotes.stream().map(quote -> {
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
			for (Map<String, Object> map : tagMappings) {
				Long quoteId = ((Number) map.get("quote_id")).longValue();
				Long tagId = ((Number) map.get("tag_id")).longValue();
				String tagName = (String) map.get("tag_name");

				Tag tag = new Tag();
				tag.setId(tagId);
				tag.setName(tagName);

				quoteTagMap.computeIfAbsent(quoteId, k -> new ArrayList<>()).add(tag);
			}

			// 设置 tags 字段
			for (QuoteVo quoteVo : quoteVoList) {
				quoteVo.setCategoryName(categoryMap.getOrDefault(quoteVo.getCategoryId(), "其他"));
				quoteVo.setTags(quoteTagMap.getOrDefault(quoteVo.getId(), new ArrayList<>()));
			}
			//quoteVoList转mapList
			mapList = quoteVoList.stream().map(quoteVo -> {
				Map<String, Object> map = new HashMap<>();
				map.put("id", quoteVo.getId());
				map.put("content", quoteVo.getContent());
				map.put("author", quoteVo.getAuthor());
				map.put("source", quoteVo.getSource());
				map.put("categoryName", quoteVo.getCategoryName());
				map.put("tags", quoteVo.getTags());
				return map;
			}).collect(Collectors.toList());
		}
		return Result.success(mapList);
	}

	@Override
	public String getQuoteType(String quoteId) {
		int quoteType = quoteMapper.getQuoteType(quoteId);
		return quoteType == 1 ? "ORIGINAL" : "NON-ORIGINAL";
	}

	@Override
	public boolean checkQuoteExists(Map<String, String> request) {
		String quoteId = request.get("quoteId");
		return quoteMapper.checkQuoteExists(quoteId);
	}

	@SuppressWarnings("unchecked")
	private Result<Page<QuoteVo>> queryVerify(PageDto pageDto) {
		LambdaQueryWrapper<QuoteVerify> queryWrapper = new LambdaQueryWrapper<QuoteVerify>()
				.orderByDesc(QuoteVerify::getUpdatedTime);
		int page = pageDto.getPage();
		int size = pageDto.getSize();
		Page<QuoteVerify> pageData = quoteVerifyService.page(new Page<>(page, size), queryWrapper);

		List<QuoteVerify> records = pageData.getRecords();

		Page<QuoteVo> pageVo = new Page<>(pageData.getCurrent(), pageData.getSize(), pageData.getTotal());

		if (!records.isEmpty()) {
			List<QuoteVo> quoteVoList = records.stream().map(quote -> {
				QuoteVo quoteVo = new QuoteVo();
				BeanUtils.copyProperties(quote, quoteVo);
				return quoteVo;
			}).collect(Collectors.toList());

			List<Long> categoryIds = quoteVoList.stream().map(QuoteVo::getCategoryId).collect(Collectors.toList());


			List<String> tags = records.stream().map(QuoteVerify::getTag).collect(Collectors.toList());
			Set<Long> hashSet = new HashSet<>();
			for (String tag : tags) {
				String[] tagArray = tag.split(",");
				for (String tagId : tagArray) {
					hashSet.add(Long.valueOf(tagId));
				}
			}
			List<Tag> tagList = tagService.listByIds(hashSet);
			Map<Long, String> tagMap = tagList.stream().collect(Collectors.toMap(Tag::getId, Tag::getName));

			Map<Long, List<Tag>> quoteTagMap = new HashMap<>();
			records.forEach(record -> {
				Long recordId = record.getId();
				String tag = record.getTag();
				String[] tagArray = tag.split(",");
				for (String tagId : tagArray) {
					Long tagIdLong = Long.valueOf(tagId);
					String tagName = tagMap.get(tagIdLong);
					Tag tagObj = new Tag();
					tagObj.setId(tagIdLong);
					tagObj.setName(tagName);
					quoteTagMap.computeIfAbsent(recordId, k -> new ArrayList<>()).add(tagObj);
				}
			});

			Map<Long, String> categoryMap = new HashMap<>();
			if (!categoryIds.isEmpty()) {
				List<Category> categoryList = categoryService.list(new LambdaQueryWrapper<Category>().in(Category::getId, categoryIds));
				categoryMap = categoryList.stream().collect(Collectors.toMap(Category::getId, Category::getName));
			}

			// 设置 tags 字段和分类字段
			for (QuoteVo quoteVo : quoteVoList) {
				quoteVo.setCategoryName(categoryMap.get(quoteVo.getCategoryId()));
				quoteVo.setTags(quoteTagMap.getOrDefault(quoteVo.getId(), new ArrayList<>()));
			}
			pageVo.setRecords(quoteVoList);
		}
		return Result.success(pageVo);
	}
	public void saveOrUpdateQuoteTags(List<QuoteTag> quoteTags) {
		// 先删除原有关系
		LambdaQueryWrapper<QuoteTag> wrapper = new LambdaQueryWrapper<QuoteTag>()
				.eq(QuoteTag::getQuoteId, quoteTags.get(0).getQuoteId());
		quoteTagMapper.delete(wrapper);

		// 批量插入新关系
		if (!quoteTags.isEmpty()) {
			quoteTagMapper.insertBatch(quoteTags); // 使用自定义的批量插入方法
		}
	}
}
