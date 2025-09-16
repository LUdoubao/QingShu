package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.quote.service.dto.TagCountDTO;
import org.doubao.quote.service.dto.TagCountVo;
import org.doubao.quote.service.dto.TagQuery;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.mapper.TagMapper;
import org.doubao.quote.service.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

	@Resource
	private TagMapper tagMapper;
	@Override
	public Result<List<TagCountVo>> listQuery(TagQuery tagQuery) {
		String tagName = tagQuery.getTagName();
		List<Long> ids = tagQuery.getIds();
		List<TagCountVo> tagCountVos  = new ArrayList<>();
		if ((tagName == null || tagName.isEmpty()) && (ids == null || ids.isEmpty())) {
			// 查询被绑定的标签最多的10条
			tagCountVos = tagMapper.selectTopTags(10);
		} else {
			if (tagName != null && !tagName.isEmpty()) {
				// 按名称模糊查询
				List<Tag> list = this.list(new LambdaQueryWrapper<Tag>().like(Tag::getName, tagName));
				tagCountVos = list.stream().map(tag -> {
					TagCountVo tagCountVo = new TagCountVo();
					tagCountVo.setId(tag.getId());
					tagCountVo.setName(tag.getName());
					tagCountVo.setQuoteCount(0L);
					return tagCountVo;
				}).collect(Collectors.toList());
			} else {
				// 按id查询
				tagCountVos = this.listByIds(ids).stream().map(tag -> {
					TagCountVo tagCountVo = new TagCountVo();
					tagCountVo.setId(tag.getId());
					tagCountVo.setName(tag.getName());
					tagCountVo.setQuoteCount(0L);
					return tagCountVo;
				}).collect(Collectors.toList());
			}
		}
		return Result.success(tagCountVos);
	}

	@Override
	public Result<Tag> add(Tag tag) {
		String name = tag.getName();
		if (name == null || name.isEmpty()) {
			throw new BusinessException(ErrorCode.TAG_NAME_EMPTY);
		}
		// 校验标签名称唯一性
		if (this.count(new QueryWrapper<Tag>().eq("name", name)) > 0) {
			throw new BusinessException(ErrorCode.TAG_NAME_EXIST);
		}
		return Result.success(this.save(tag) ? tag : null);
	}

	@Override
	public List<Tag> listTopTagsByQuoteCount(String keyword, int limit) {
		// 1. 查询符合条件的标签并统计引用次数
		List<TagCountDTO> tagCounts = tagMapper.selectTagWithQuoteCount(keyword);

		// 2. 按引用次数降序排序
		tagCounts.sort((t1, t2) -> t2.getQuoteCount() - t1.getQuoteCount());

		// 3. 取前limit个标签ID
		List<Long> topTagIds = tagCounts.stream()
				.limit(limit)
				.map(TagCountDTO::getTagId)
				.collect(Collectors.toList());

		// 4. 查询完整的标签对象
		if (topTagIds.isEmpty()) return Collections.emptyList();
		List<Tag> tags = tagMapper.selectBatchIds(topTagIds);
		return tags.isEmpty() ? Collections.emptyList() : tags;
	}

	@Override
	public List<Long> listTagIdsByName(String keyword) {
		if (keyword != null && !keyword.isEmpty()) {
			return tagMapper.selectTagIdsByName(keyword);
		}
		return Collections.emptyList();
	}
}
