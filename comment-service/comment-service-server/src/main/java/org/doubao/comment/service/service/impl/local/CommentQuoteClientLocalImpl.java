package org.doubao.comment.service.service.impl.local;

import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class CommentQuoteClientLocalImpl implements QuoteClient {

	@Resource
	private QuoteService quoteService;

	@Override
	public Result<org.doubao.comment.service.vo.QuoteVo> detail(Long id) {
		try {
			QuoteVo data = quoteService.getDetailById(id).getData();
			org.doubao.comment.service.vo.QuoteVo quoteVo = new org.doubao.comment.service.vo.QuoteVo();
			BeanUtils.copyProperties(data, quoteVo);
			return Result.success(quoteVo);
		} catch (Exception e) {
			return Result.error("获取引文详情失败: " + e.getMessage());
		}
	}
}