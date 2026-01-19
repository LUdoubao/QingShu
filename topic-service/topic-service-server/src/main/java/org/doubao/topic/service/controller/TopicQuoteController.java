package org.doubao.topic.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.dto.TopicQuoteVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.dto.TopicQuoteQueryDTO;
import org.doubao.topic.service.service.QuoteTopicService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/topicQuote")
public class TopicQuoteController {
	@Resource
	private QuoteTopicService topicQuoteService;

	/**
	 * 查询话题下的所有关联的引文
	 * @param queryDTO 查询参数
	 * @return 查询结果
	 */
	@PostMapping("/list")
	public Result<Page<TopicQuoteVO>> queryTopicQuotes(@RequestBody TopicQuoteQueryDTO queryDTO) {
		return Result.success(topicQuoteService.queryTopicQuotes(queryDTO));
	}
}
