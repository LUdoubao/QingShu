package org.doubao.comment.service.service.impl.local;

import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 评论服务本地引文客户端实现
 * <p>
 * 在单体模式下（service.run-mode=monolith）的引文客户端实现，
 * 通过直接调用本地QuoteService来获取引文信息，
 * 避免了微服务间的网络调用，提高性能
 */
@Service
@MonolithMode
public class CommentQuoteClientLocalImpl implements QuoteClient {

	@Resource
	@Lazy
	private QuoteService quoteService;

	/**
	 * 获取引文详情
	 * <p>
	 * 在单体模式下直接调用本地QuoteService获取引文详情，
	 * 并将结果转换为评论服务所需的QuoteVo格式
	 * 
	 * @param id 引文ID
	 * @return 包含引文详情的成功响应，或包含错误信息的失败响应
	 */
	@Override
	public Result<org.doubao.comment.service.vo.QuoteVo> detail(Long id) {
		try {
			QuoteVo data = quoteService.publicGetDetailById(id, null).getData();
			org.doubao.comment.service.vo.QuoteVo quoteVo = convert(data);
			return Result.success(quoteVo);
		} catch (Exception e) {
			return Result.error("获取引文详情失败: " + e.getMessage());
		}
	}
	private org.doubao.comment.service.vo.QuoteVo convert(QuoteVo data) {
		org.doubao.comment.service.vo.QuoteVo quoteVo = new org.doubao.comment.service.vo.QuoteVo();
		if (data != null) {
			quoteVo.setId(data.getId());
			quoteVo.setContent(data.getContent());
			quoteVo.setAuthor(data.getAuthor());
			quoteVo.setSource(data.getSource());
			quoteVo.setCategoryName(data.getCategoryName());
			quoteVo.setCategoryId(data.getCategoryId());
			quoteVo.setCreatedId(data.getCreatedId());
		}
		return quoteVo;
	}
}