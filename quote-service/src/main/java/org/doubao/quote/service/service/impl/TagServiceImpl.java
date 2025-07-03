package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.mapper.TagMapper;
import org.doubao.quote.service.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

	@Override
	public Result<List<Tag>> listByTagName(String tagName) {
		List<Tag> list = this.list(new LambdaQueryWrapper<Tag>().like(Tag::getName, tagName == null ? "" : tagName));
		return Result.success(list);
	}
}
