package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.TagCountVo;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.mapper.TagMapper;
import org.doubao.quote.service.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

	@Resource
	private TagMapper tagMapper;
	@Override
	public Result<List<TagCountVo>> listByTagName(String tagName) {
		List<TagCountVo> tagCountVos  = new ArrayList<>();
		if (tagName == null) {
			// 查询被绑定的标签最多的10条
			tagCountVos = tagMapper.selectTopTags(10);
		} else {
			List<Tag> list = this.list(new LambdaQueryWrapper<Tag>().like(Tag::getName, tagName));
			tagCountVos = list.stream().map(tag -> {
				TagCountVo tagCountVo = new TagCountVo();
				tagCountVo.setId(tag.getId());
				tagCountVo.setName(tag.getName());
				tagCountVo.setQuoteCount(0L);
				return tagCountVo;
			}).collect(Collectors.toList());
		}
		return Result.success(tagCountVos);
	}
}
