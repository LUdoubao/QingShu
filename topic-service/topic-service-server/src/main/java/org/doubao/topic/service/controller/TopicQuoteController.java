package org.doubao.topic.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.dto.TopicBindDTO;
import org.doubao.mall.common.dto.TopicQuoteVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.dto.TopicQuoteQueryDTO;
import org.doubao.topic.service.service.QuoteTopicService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
	@PostMapping("/deleteQuoteBind")
	public Result<Boolean> deleteQuoteBind(@RequestBody List<Long> quoteIds) {
		topicQuoteService.deleteQuoteBind(quoteIds);
		return Result.success(true);
	}

	/**
	 * 绑定文案到话题
	 * 建立文案与话题的关联关系
	 *
	 * @param dto 绑定DTO
	 * @return 操作结果
	 */
	@PostMapping("/bind-quote")
	public Result<Boolean> bindQuoteToTopic(@RequestBody TopicBindDTO dto) {
		topicQuoteService.bindQuoteToTopic(dto);
		return Result.success(true);
	}

	@PostMapping("/update-bind-quote")
	public Result<Boolean> updateBindQuote(@RequestBody TopicBindDTO dto) {
		topicQuoteService.updateQuoteBind(dto);
		return Result.success(true);
	}
}
