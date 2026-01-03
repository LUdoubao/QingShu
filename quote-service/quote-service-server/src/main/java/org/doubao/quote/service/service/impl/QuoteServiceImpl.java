package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.ResultCode;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QueryDataPageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.duplicate.check.CitationCheckService;
import org.doubao.quote.service.duplicate.check.DecisionEngine;
import org.doubao.quote.service.entity.*;
import org.doubao.quote.service.enums.QuoteStatus;
import org.doubao.quote.service.feign.*;
import org.doubao.quote.service.mapper.QuoteMapper;
import org.doubao.quote.service.mapper.QuoteTagMapper;
import org.doubao.quote.service.messaging.QuoteEventPublisher;
import org.doubao.quote.service.service.*;
import org.doubao.quote.service.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl extends ServiceImpl<QuoteMapper, Quote> implements QuoteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteServiceImpl.class);
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
	private UserClient userClient;
	@Resource
	private ViewCountClient viewCountClient;
	@Resource
	private LikeClient likeClient;
	@Resource
	private CommentClient commentClient;
	@Resource
	 private FavoriteClient favoriteClient;
	@Resource
	private CitationCheckService citationCheckService;
	@Override
	@SuppressWarnings("unchecked")
	public Result<Page<QuoteVo>> page(PageDto pageDto) {
		return query(pageDto);
	}

	@Override
	public Result<Void> addQuote(QuoteDTO dto) {
		// 检查引文是否重复
		DecisionEngine.DuplicationResult duplicationResult = citationCheckService.checkCitation(dto.getContent(), dto.getAuthor(), dto.getSource(), dto.getOriginal() == 1);
		if (duplicationResult.getStatus() == DecisionEngine.DuplicationStatus.DUPLICATE) {
			throw new BusinessException(ErrorCode.CONTENT_EXISTS);
		}
		Quote q = new Quote();
		Long quoteId = null;
		if (dto.getId() != null) {
			quoteId = dto.getId();
			q.setId(dto.getId());
		}
		q.setContent(dto.getContent());
		q.setAuthor(dto.getAuthor());
		q.setSource(dto.getSource());
		q.setCategoryId(dto.getCategoryId());
		q.setOriginal(dto.getOriginal());
		// 默认引文状态为待审核
		q.setStatus(QuoteStatus.AUDITING.getCode());
		this.saveOrUpdate(q);
		Long qId = q.getId();
		if (quoteId != null) {
			quoteTagMapper.deleteByQuoteId(quoteId);
		}
		List<Long> tagIds = dto.getTagIds();
		List<Tag> tags = new ArrayList<>();
		List<QuoteTag> quoteTags = new ArrayList<>();
		if (tagIds != null && !tagIds.isEmpty()) {
			tags = tagService.listByIds(tagIds);
			for (Long tagId : tagIds) {
				QuoteTag qt = new QuoteTag();
				qt.setQuoteId(qId);
				qt.setTagId(tagId);
				quoteTags.add(qt);
			}
			quoteTagMapper.insertBatch(quoteTags);
		}

		// 同步到审核表
		QuoteVerify quoteVerify = new QuoteVerify();
		BeanUtils.copyProperties(q,quoteVerify);
		quoteVerify.setUpdatedTime(LocalDateTime.now());
		if (tags != null && !tags.isEmpty()) {
			String tag = tags.stream().map(Tag::getId)
					.map(String::valueOf)
					.collect(Collectors.joining(","));
			quoteVerify.setTag(tag);
		}
		quoteVerifyService.save(quoteVerify);
		// 推送待审核消息到管理员消息中心
		quoteEventPublisher.pushQuoteUpdateNotification(1L,
				q.getId(), dto.getContent(), q.getCreatedId());
		return Result.success();
	}

	@Override
	public Result<String> deleteQuote(List<Long> quoteIds) {
		if (quoteIds == null || quoteIds.isEmpty()) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		this.removeByIds(quoteIds);
		quoteTagMapper.deleteByQuoteIds(quoteIds);
		return Result.success(ResultCode.SUCCESS.getMessage());
	}

	@Override
	public Result<String> updateQuote(QuoteUpdateDto dto) {
		UserLoginVo userInfo = UserContext.getUser();
		QuoteVo afterQuoteVo = dto.getAfterQuoteVo();
		Quote quote = new Quote();
		BeanUtils.copyProperties(afterQuoteVo, quote);
		List<Tag> tags = afterQuoteVo.getTags();
		if (userInfo.getId() == 1 && userInfo.getUsername().equals("admin")) {
			// 管理员直接保存
			quote.setStatus(QuoteStatus.PUBLISHED.getCode());
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
			updateWrapper.set(Quote::getStatus, QuoteStatus.AUDITING.getCode());
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

			// 推送待审核消息到管理员消息中心
			quoteEventPublisher.pushQuoteUpdateNotification(1L,
					quoteId, dto.getAfterQuoteVo().getContent(), quote.getCreatedId());
		}

		return Result.success(ResultCode.SUCCESS.getMessage());
	}

	@Override
	@SuppressWarnings("unchecked")
	public Result<QuoteVo> getDetailById(Long id) {
		QuoteVo quoteVo = publicGetDetailById(id, null).getData();
		if (quoteVo == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND);
		}
		Long currentUserId = UserContext.getUserId();
		Long createdId = quoteVo.getCreatedId();
		Map<Long, Boolean> followMap	 = userClient.isFollow(currentUserId, Collections.singleton(createdId)).getData();

		// 设置是否关注
		quoteVo.setFollow(followMap.getOrDefault(quoteVo.getCreatedId(), false));
		return Result.success(quoteVo);
	}
	@Override
	public Result<QuoteVo> publicGetDetailById(Long id, List<Integer> statusList) {
		if (statusList == null || statusList.isEmpty()) {
			statusList = Collections.singletonList(1);
			LOGGER.info("statusList is empty, use default statusList: {}", statusList);
		}
		LambdaQueryWrapper<Quote> queryWrapper = new LambdaQueryWrapper<Quote>()
				.eq(Quote::getDeleted, 0)
				.eq(Quote::getId, id)
				.in(Quote::getStatus,statusList);
		Quote quote = this.getOne(queryWrapper);
		if (quote == null) {
			// 引文不存在或待审核
			throw new BusinessException(ErrorCode.QUOTE_NOT_FOUND);
		}
		QuoteVo quoteVo = new QuoteVo();
		BeanUtils.copyProperties(quote, quoteVo);
		Long createdId = quoteVo.getCreatedId();
		List<UserInfoDes> userInfos = userClient.getUsersByIds(Collections.singleton(createdId)).getData();

		List<Map<String, Object>> tagMappings = quoteTagMapper.selectQuoteTagsWithDetails(Collections.singletonList(id));
		Map<Long, List<Tag>> quoteTagMap = new HashMap<>();
		if (tagMappings != null && !tagMappings.isEmpty()) {
			for (Map<String, Object> map : tagMappings) {
				Long quoteId = ((Number) map.get("quote_id")).longValue();
				Long tagId = ((Number) map.get("tag_id")).longValue();
				String tagName = (String) map.get("tag_name");

				Tag tag = new Tag();
				tag.setId(tagId);
				tag.setName(tagName);

				quoteTagMap.computeIfAbsent(quoteId, k -> new ArrayList<>()).add(tag);
			}
		}

		Long categoryId = quoteVo.getCategoryId();
		Category category = categoryService.getById(categoryId);

		quoteVo.setCategoryName(category == null ? "" : category.getName());
		quoteVo.setTags(quoteTagMap.getOrDefault(quoteVo.getId(), new ArrayList<>()));
		Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> String.valueOf(userInfo.getId()).equals(String.valueOf(quoteVo.getCreatedId()))).findFirst();
		first.ifPresent(quoteVo::setUserInfo);
		return Result.success(quoteVo);
	}

	@Override
	public Result<Page<QuoteVo>> pageManager(PageDto pageDto) {
		Long userId = UserContext.getUser().getId();
		if (userId!= null && userId != 1) {
			pageDto.setUserId(userId);
		}
		return query(pageDto);
	}

	private Result<Page<QuoteVo>> query(PageDto pageDto) {
		Long currentUserId = UserContext.getUser() == null ? pageDto.getCurrentUserId() : UserContext.getUser().getId();
		int page = pageDto.getPage();
		int size = pageDto.getSize();
		Long categoryId = pageDto.getCategoryId();
		List<Long> tagIds = pageDto.getTagIds();
		Long userId = pageDto.getUserId();
		Integer original = pageDto.getOriginal();
		String quoteKeyword = pageDto.getQuoteKeyword();

		// 1. 查询总数
		long total = quoteMapper.countByTagIdsAndCategory(categoryId, tagIds, tagIds == null ? 0 : tagIds.size(),
				userId, original, 1, quoteKeyword);

		// 2. 查询分页数据
		List<Quote> records = quoteMapper.selectByTagIdsAndCategory(
				categoryId,
				tagIds,
				tagIds == null ? 0 : tagIds.size(),
				size,
				(page - 1) * size,
				userId,
				original,
				1,
				quoteKeyword
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

			// 获取创建者信息
			Set<Long> createdIds = quoteVoList.stream().map(QuoteVo::getCreatedId).collect(Collectors.toSet());
			List<UserInfoDes> userInfos = userClient.getUsersByIds(createdIds).getData();
			// 获取关注信息
			Map<Long, Boolean> followMap	 = userClient.isFollow(currentUserId, createdIds).getData();

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
				// 设置用户信息
				Optional<UserInfoDes> first = userInfos.stream().filter(userInfo -> String.valueOf(userInfo.getId()).equals(String.valueOf(quoteVo.getCreatedId()))).findFirst();
				first.ifPresent(quoteVo::setUserInfo);
				// 设置是否关注
				quoteVo.setFollow(followMap.getOrDefault(quoteVo.getCreatedId(), false));
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
		UserLoginVo user = UserContext.getUser();
		if (status == 0) {// 审核不通过
			// 引文表状态恢复1
			LambdaUpdateWrapper<Quote> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(Quote::getId, quoteId)
					.set(Quote::getStatus, QuoteStatus.NOT_PASS.getCode());
			this.update(updateWrapper);

			quoteEventPublisher.pushQuoteVerifyNotification(
					quoteId,
					dto.getContent(),
					"REJECTED",
					"审核驳回",
					"管理员",
					dto.getCreatedId()
			);
		} else {
			Quote quote = this.getById(quoteId);
			quote.setStatus(QuoteStatus.PUBLISHED.getCode());
			QuoteVerify quoteVerify = quoteVerifyService.getById(quoteId);
			String verifyTag = quoteVerify.getTag();
			List<QuoteTag> quoteTags = new ArrayList<>();
			if (verifyTag != null && !verifyTag.isEmpty()) {
				// ,号分割
				String[] tags = verifyTag.split(",");
				for (String tagId : tags) {
					QuoteTag qt = new QuoteTag();
					qt.setQuoteId(quoteId);
					qt.setTagId(Long.valueOf(tagId));
					quoteTags.add(qt);
				}
				saveOrUpdateQuoteTags(quoteTags);
			}
			quote.setSource(quoteVerify.getSource());
			quote.setAuthor(quoteVerify.getAuthor());
			quote.setCategoryId(quoteVerify.getCategoryId());
			quote.setContent(quoteVerify.getContent());
			this.updateById(quote);
			quoteEventPublisher.pushQuoteVerifyNotification(
					quoteId,
					dto.getContent(),
					"APPROVED",
					"审核通过",
					user.getUsername(),
					dto.getCreatedId()
			);
			quoteEventPublisher.pushFanoutFeedPublish(quote);
		}
		// 删除审核表数据
		LambdaQueryWrapper<QuoteVerify> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(QuoteVerify::getId, quoteId);
		quoteVerifyService.remove(wrapper);
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
				.in(Quote::getId, ids)
				.eq(Quote::getDeleted, 0)
				.eq(Quote::getStatus,  QuoteStatus.PUBLISHED.getCode());
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
				map.put("createdId", quoteVo.getCreatedId());
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

	@Override
	public Result<Page<QuoteVo>> originalPage(PageDto pageDto) {
		// 校验查询权限feign
		Long targetUserId = pageDto.getUserId();
		Long currentUserId = UserContext.getUserId();
		Boolean check = userClient.checkWorkPermission(targetUserId, currentUserId).getData();
		if (!check) {
			throw new BusinessException(ErrorCode.USER_PRIVACY_QUOTE_LIST_NOT_OPEN);
		}
		// 分页查询
		return page(pageDto);
	}

	@Override
	public Result<Map<String, String>> getSearchSuggestions(String keyword) {
		// 获取搜索建议
		Map<String, String> stringStringMap = new HashMap<>();
		// 筛选被引文绑定最多的三个标签
		List<Tag> tags = tagService.listTopTagsByQuoteCount(keyword, 3);
		if (!tags.isEmpty()) {
			// 获取id列表
			stringStringMap.put("tags", tags.stream().map(Tag::getName).collect(Collectors.joining(",")));
		}
		// 筛选被引文绑定最多的三个分类
		// List<CategoryCountVO> topCategoriesByKeyword = getTopCategoriesByKeyword(keyword);
		// if (!topCategoriesByKeyword.isEmpty()) {
		// 	stringStringMap.put("categories", topCategoriesByKeyword.stream().map(CategoryCountVO::getCategoryName).collect(Collectors.joining(",")));
		// }
		int quoteCount = 9 - tags.size();

		// 查询 quoteCount条随机的quote
		List<Quote> quotes = quoteMapper.selectRandomQuotes(keyword, quoteCount);

		if (!quotes.isEmpty()) {
			stringStringMap.put("quotes", quotes.stream().map(Quote::getContent).collect(Collectors.joining(",")));
		}

		return Result.success(stringStringMap);
	}

	@Override
	public Result<Map<String, Object>> search(String keyword, int page, int size, String type,  Long currentUserId) {
		PageDto pageDto = new PageDto();
		pageDto.setPage(page);
		pageDto.setSize(size);
		pageDto.setCurrentUserId(currentUserId);
		Page<QuoteVo> quoteVoPage = new Page<>();
		switch(type) {
			case "quote":
				pageDto.setQuoteKeyword(keyword);
				quoteVoPage = page(pageDto).getData();
				break;
			case "tag":
				// 根据keyword获取标签id
				List<Long> tagIds = tagService.listTagIdsByName(keyword);
				pageDto.setTagIds(tagIds);
				quoteVoPage = page(pageDto).getData();
				break;
			default:
				LOGGER.error("Invalid search type: {}", type);
				break;
		}
		// 转换为Map<String, Object>
		if (quoteVoPage != null && !quoteVoPage.getRecords().isEmpty()) {
			Map<String, Object> map = new HashMap<>();
			map.put("total", quoteVoPage.getTotal());
			map.put("records", quoteVoPage.getRecords());
			return Result.success(map);
		}
		LOGGER.info("No quotes found for keyword: {}", keyword);
		return Result.success();
	}

	@Override
	public void updateStatus(Map<String, String> request) {
		Long quoteId = Long.valueOf(request.get("quoteId"));
		int status = Integer.parseInt(request.get("status"));
		LambdaUpdateWrapper<Quote> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(Quote::getStatus, status)
				.eq(Quote::getId, quoteId);
		this.update(updateWrapper);
	}

	@Override
	public Page<QuoteDataVo> queryQuoteData(QueryDataPageDto queryDataPageDto) {
		Integer page = queryDataPageDto.getPage();
		Integer size = queryDataPageDto.getSize();
		Integer original = queryDataPageDto.getOriginal();
		String quoteKeyword = queryDataPageDto.getQuoteKeyword();
		Integer status = queryDataPageDto.getStatus();
		Long userId = UserContext.getUserId();
		// 1. 查询总数
		long total = quoteMapper.countByTagIdsAndCategory(null, null, 0,
				userId, original, status, quoteKeyword);

		// 2. 查询分页数据
		List<Quote> records = quoteMapper.selectByTagIdsAndCategory(
				null,
				null,
				0,
				size,
				(page - 1) * size,
				userId,
				original,
				status,
				quoteKeyword
		);

		Page<QuoteDataVo> pageVo = new Page<>(page, size, total);
		if (!records.isEmpty()) {
			List<QuoteDataVo> quoteVoList = records.stream().map(quote -> {
				QuoteDataVo quoteDataVo = new QuoteDataVo();
				BeanUtils.copyProperties(quote, quoteDataVo);
				return quoteDataVo;
			}).collect(Collectors.toList());

			List<Long> quoteIds = quoteVoList.stream().map(QuoteDataVo::getId).collect(Collectors.toList());

			// 标签
			List<Map<String, Object>> tagMappings = quoteTagMapper.selectQuoteTagsWithDetails(quoteIds);
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

			// 浏览量
			Map<Long, Long> batchGetViewCounts = viewCountClient.batchGetViewCounts(quoteIds).getData();
			// 点赞量
			Map<Long, Long> likeCounts = likeClient.batchGetCounts(quoteIds).getData();
			// 评论量
			Map<Long, Long> commentCounts = commentClient.batchGetCounts(quoteIds).getData();
			for (QuoteDataVo quoteVo : quoteVoList) {
				quoteVo.setTags(quoteTagMap.getOrDefault(quoteVo.getId(), new ArrayList<>()));
				quoteVo.setViewCount(batchGetViewCounts.getOrDefault(quoteVo.getId(), 0L));
				quoteVo.setLikeCount(likeCounts.getOrDefault(quoteVo.getId(), 0L));
				quoteVo.setCommentCount(commentCounts.getOrDefault(quoteVo.getId(), 0L));
			}
			pageVo.setRecords(quoteVoList);
		}
		return pageVo;
	}

	@Override
	public QuoteStatusCountVo queryStatusCount() {
		Long userId = UserContext.getUserId();
		return quoteMapper.queryStatusCount(userId);
	}

	@Override
	public ContentOverviewVo queryContentOverview() {
		// 获取当前用户ID（数据权限：仅统计当前用户的内容）
		Long userId = UserContext.getUserId();

		// 1. 查询当前用户的所有已发布文章ID（排除已删除的）
		LambdaQueryWrapper<Quote> quoteQuery = new LambdaQueryWrapper<Quote>()
				.eq(Quote::getCreatedId, userId)
				.eq(Quote::getStatus, 1) // 已发布
				.eq(Quote::getDeleted, 0) // 未删除
				.select(Quote::getId); // 仅查询ID，优化性能
		List<Quote> userQuotes = this.list(quoteQuery);
		List<Long> quoteIds = userQuotes.stream()
				.map(Quote::getId)
				.collect(Collectors.toList());

		// 2. 计算总文章数
		long totalArticles = userQuotes.size();

		// 3. 计算总浏览量（通过ViewCountClient获取）
		long totalViews = 0;
		if (!quoteIds.isEmpty()) {
			Map<Long, Long> viewCountMap = viewCountClient.batchGetViewCounts(quoteIds).getData();
			if (viewCountMap != null) {
				totalViews = viewCountMap.values().stream().mapToLong(Long::longValue).sum();
			}
		}

		// 4. 计算总点赞数（通过LikeClient获取）
		long totalLikes = 0;
		if (!quoteIds.isEmpty()) {
			Map<Long, Long> likeCountMap = likeClient.batchGetCounts(quoteIds).getData();
			if (likeCountMap != null) {
				totalLikes = likeCountMap.values().stream().mapToLong(Long::longValue).sum();
			}
		}

		// 5. 计算总评论数（通过CommentClient获取）
		long totalComments = 0;
		if (!quoteIds.isEmpty()) {
			Map<Long, Long> commentCountMap = commentClient.batchGetCounts(quoteIds).getData();
			if (commentCountMap != null) {
				totalComments = commentCountMap.values().stream().mapToLong(Long::longValue).sum();
			}
		}

		// 6. 计算总收藏数
		long totalFavorites = 0;
		if (!quoteIds.isEmpty()) {
			Map<Long, Long> favoriteCountMap = favoriteClient.countQuotes(quoteIds).getData();
			if (favoriteCountMap != null) {
				totalFavorites = favoriteCountMap.values().stream().mapToLong(Long::longValue).sum();
			}
		}

		// 组装结果VO
		ContentOverviewVo overviewVo = new ContentOverviewVo();
		overviewVo.setTotalArticles(totalArticles);
		overviewVo.setTotalViews(totalViews);
		overviewVo.setTotalLikes(totalLikes);
		overviewVo.setTotalComments(totalComments);
		overviewVo.setTotalFavorites(totalFavorites);

		return overviewVo;
	}

	@Override
	public List<ContentTrendVo> queryContentTrend(int days, List<String> metrics) {
		// 1. 参数校验
		if (days <= 0) {
			throw new BusinessException(ErrorCode.CONTENT_STATISTICS_DAYS_INVALID);
		}
		// 处理空指标列表（默认统计所有指标）
		if (metrics == null || metrics.isEmpty()) {
			metrics = Arrays.asList("views", "likes", "comments", "favorites");
		} else {
			// 过滤无效指标
			metrics = metrics.stream()
					.filter(metric -> Arrays.asList("views", "likes", "comments", "favorites").contains(metric))
					.collect(Collectors.toList());
		}

		// 2. 获取当前用户ID（仅统计当前用户的内容数据）
		Long userId = UserContext.getUserId();

		// 3. 计算日期范围（包含今天在内的最近days天）
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days - 1); // 起始日期 = 今天 - (天数-1)
		List<LocalDate> dateList = generateDateList(startDate, endDate); // 生成连续日期列表

		List<Long> quoteIds = getQuoteIdsInDateRange(userId, startDate, endDate);
		if (quoteIds.isEmpty()) {
			// 无文章数据时，返回全0趋势
			return dateList.stream().map(date -> {
				ContentTrendVo vo = new ContentTrendVo();
				vo.setDate(date);
				vo.setViews(0L);
				vo.setLikes(0L);
				vo.setComments(0L);
				vo.setFavorites(0L);
				return vo;
			}).collect(Collectors.toList());
		}

		// 4. 批量查询所有日期的指标数据（核心优化：按指标批量查询，而非按日期逐个查询）
		Map<LocalDate, Long> dailyViews = new HashMap<>();
		Map<LocalDate, Long> dailyLikes = new HashMap<>();
		Map<LocalDate, Long> dailyComments = new HashMap<>();
		Map<LocalDate, Long> dailyFavorites = new HashMap<>();

		// 4.1 批量查询浏览量（一次调用获取所有日期数据）
		if (metrics.contains("views")) {
			dailyViews = batchSumDailyViewCounts(quoteIds, dateList);
		}
		// 4.2 批量查询点赞数
		if (metrics.contains("likes")) {
			dailyLikes = batchSumDailyLikeCounts(quoteIds, dateList);
		}
		// 4.3 批量查询评论数
		if (metrics.contains("comments")) {
			dailyComments = batchSumDailyCommentCounts(quoteIds, dateList);
		}
		// 4.4 批量查询收藏数
		if (metrics.contains("favorites")) {
			dailyFavorites = batchSumDailyCollectionCounts(quoteIds, dateList);
		}

		// 5. 组装每日趋势数据（从批量结果中取值）
		List<ContentTrendVo> trendVos = new ArrayList<>();
		for (LocalDate date : dateList) {
			ContentTrendVo trendVo = new ContentTrendVo();
			trendVo.setDate(date);
			// 从批量结果中获取对应日期的数据，无数据则为0
			trendVo.setViews(metrics.contains("views") ? dailyViews.getOrDefault(date, 0L) : 0L);
			trendVo.setLikes(metrics.contains("likes") ? dailyLikes.getOrDefault(date, 0L) : 0L);
			trendVo.setComments(metrics.contains("comments") ? dailyComments.getOrDefault(date, 0L) : 0L);
			trendVo.setFavorites(metrics.contains("favorites") ? dailyFavorites.getOrDefault(date, 0L) : 0L);
			trendVos.add(trendVo);
		}

		return trendVos;
	}

	@Override
	public QuoteVo getUpdateDetail(Long id) {
		// 仅获取已发布, 未通过, 草稿,下架的文章
		List<Integer> statusList = Arrays.asList(QuoteStatus.PUBLISHED.getCode(),
				QuoteStatus.DRAFT.getCode(),
				QuoteStatus.NOT_PASS.getCode(),
				QuoteStatus.OFF_SHELF.getCode());
		return publicGetDetailById(id, statusList).getData();
	}

	@Override
	public void offOrOnShelf(Long quoteId, Integer status) {
		if (status == null || QuoteStatus.getQuoteStatus(status) == null) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		QuoteStatus quoteStatus = QuoteStatus.getQuoteStatus(status);
		if (Objects.requireNonNull(quoteStatus) == QuoteStatus.OFF_SHELF) {// 下架
			quoteMapper.updateQuoteStatus(quoteId, QuoteStatus.OFF_SHELF.getCode());
		} else {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
	}

	@Override
	public Long saveAsDraft(QuoteDTO dto) {
		Long id = dto.getId();
		if (id != null) {
			Quote quote = this.getById(id);
			if (quote == null) {
				throw new BusinessException(ErrorCode.NOT_FOUND);
			}
			if (quote.getStatus() != QuoteStatus.DRAFT.getCode()) {
				throw new BusinessException(ErrorCode.BAD_REQUEST);
			}
			quote.setContent(dto.getContent());
			quote.setAuthor(dto.getAuthor());
			quote.setSource(dto.getSource());
			quote.setCategoryId(dto.getCategoryId());
			quote.setOriginal(dto.getOriginal());
			this.updateById(quote);


			// 删除旧标签
			quoteTagMapper.deleteByQuoteId(id);
			// 添加新标签
			List<Long> tagIds = dto.getTagIds();
			List<QuoteTag> quoteTags = new ArrayList<>();
			if (tagIds != null && !tagIds.isEmpty()) {
				for (Long tagId : tagIds) {
					QuoteTag qt = new QuoteTag();
					qt.setQuoteId(id);
					qt.setTagId(tagId);
					quoteTags.add(qt);
				}
				quoteTagMapper.insertBatch(quoteTags);
			}
			return id;
		}
		Quote q = new Quote();
		q.setContent(dto.getContent());
		q.setAuthor(dto.getAuthor());
		q.setSource(dto.getSource());
		q.setCategoryId(dto.getCategoryId());
		q.setOriginal(dto.getOriginal());
		q.setStatus(QuoteStatus.DRAFT.getCode());
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
		return qId;
	}

	/**
	 * 生成从startDate到endDate的连续日期列表（包含首尾）
	 */
	private List<LocalDate> generateDateList(LocalDate startDate, LocalDate endDate) {
		List<LocalDate> dates = new ArrayList<>();
		LocalDate currentDate = startDate;
		while (!currentDate.isAfter(endDate)) {
			dates.add(currentDate);
			currentDate = currentDate.plusDays(1);
		}
		return dates;
	}

	/**
	 * 获取指定日期范围内用户创建的文章ID（未删除）
	 */
	private List<Long> getQuoteIdsInDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
		LambdaQueryWrapper<Quote> queryWrapper = new LambdaQueryWrapper<Quote>()
				.eq(Quote::getCreatedId, userId) // 仅当前用户的文章
				.eq(Quote::getDeleted, 0) // 未删除
				.eq(Quote::getStatus, QuoteStatus.PUBLISHED.getCode()) // 已发布
				.select(Quote::getId); // 仅查询ID，优化性能

		return this.list(queryWrapper).stream()
				.map(Quote::getId)
				.collect(Collectors.toList());
	}

	/**
	 * 批量查询指定文章在多个日期的总浏览量
	 * @return key:日期，value:当日总浏览量
	 */
	private Map<LocalDate, Long> batchSumDailyViewCounts(List<Long> quoteIds, List<LocalDate> dates) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("quoteIds", quoteIds);
			params.put("dates", dates); // 一次性传入所有日期
			Result<Map<LocalDate, Long>> result = viewCountClient.batchSumDailyCounts(params);
			return result.isSuccess() ? result.getData() : new HashMap<>();
		} catch (Exception e) {
			LOGGER.error("批量统计浏览量异常", e);
			return new HashMap<>();
		}
	}

	/**
	 * 批量查询指定文章在多个日期的总点赞数
	 */
	private Map<LocalDate, Long> batchSumDailyLikeCounts(List<Long> quoteIds, List<LocalDate> dates) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("quoteIds", quoteIds);
			params.put("dates", dates);
			Result<Map<LocalDate, Long>> result = likeClient.batchSumDailyCounts(params);
			return result.isSuccess() ? result.getData() : new HashMap<>();
		} catch (Exception e) {
			LOGGER.error("批量统计点赞数异常", e);
			return new HashMap<>();
		}
	}

	/**
	 * 批量查询指定文章在多个日期的总评论数
	 */
	private Map<LocalDate, Long> batchSumDailyCommentCounts(List<Long> quoteIds, List<LocalDate> dates) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("quoteIds", quoteIds);
			params.put("dates", dates);
			Result<Map<LocalDate, Long>> result = commentClient.batchSumDailyCounts(params);
			return result.isSuccess() ? result.getData() : new HashMap<>();
		} catch (Exception e) {
			LOGGER.error("批量统计评论数异常", e);
			return new HashMap<>();
		}
	}

	/**
	 * 批量查询指定文章在多个日期的总收藏数
	 */
	private Map<LocalDate, Long> batchSumDailyCollectionCounts(List<Long> quoteIds, List<LocalDate> dates) {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("quoteIds", quoteIds);
			params.put("dates", dates);
			Result<Map<LocalDate, Long>> result = favoriteClient.batchSumDailyCounts(params);
			return result.isSuccess() ? result.getData() : new HashMap<>();
		} catch (Exception e) {
			LOGGER.error("批量统计收藏数异常", e);
			return new HashMap<>();
		}
	}

	public List<CategoryCountVO> getTopCategoriesByKeyword(String keyword) {
		if (StringUtils.isBlank(keyword)) {
			return Collections.emptyList();
		}
		return quoteMapper.selectTopCategoriesByKeyword(keyword);
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
