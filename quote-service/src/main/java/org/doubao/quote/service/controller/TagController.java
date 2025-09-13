package org.doubao.quote.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.TagCountVo;
import org.doubao.quote.service.dto.TagQuery;
import org.doubao.quote.service.entity.Tag;
import org.doubao.quote.service.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

	@Autowired
	private TagService tagService;

	@PostMapping("/list")
	public Result<List<TagCountVo>> list(@RequestBody TagQuery tagQuery) {
		return tagService.listQuery(tagQuery);
	}

	@PostMapping("/create")
	public Result<Tag> add(@RequestBody Tag tag) {
		return tagService.save(tag) ? Result.success(tag) : Result.error("添加失败");
	}

}

