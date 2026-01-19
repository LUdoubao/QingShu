package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.dto.TopicQuoteVO;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.dto.TopicBindDTO;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.topic.service.dto.TopicQuoteQueryDTO;
import org.doubao.topic.service.entity.QuoteTopic;
import org.doubao.topic.service.entity.Topic;
import org.doubao.topic.service.feign.QuoteClient;
import org.doubao.topic.service.mapper.QuoteTopicMapper;
import org.doubao.topic.service.mapper.TopicMapper;
import org.doubao.topic.service.service.QuoteTopicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuoteTopicServiceImpl extends ServiceImpl<QuoteTopicMapper, QuoteTopic> implements QuoteTopicService {
	@Resource
	private TopicMapper topicMapper;
	@Resource
	private QuoteClient quoteClient;
	@Override
	public void deleteQuoteBind(List<Long> quoteIds) {
		if (quoteIds == null || quoteIds.isEmpty()) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		this.remove(new LambdaQueryWrapper<QuoteTopic>()
				.in(QuoteTopic::getQuoteId, quoteIds));
	}

	@Override
	@Transactional
	public void bindQuoteToTopic(TopicBindDTO dto) {
		// 检查话题是否存在且已发布
		Topic topic = topicMapper.selectById(dto.getTopicId());
		if (topic == null || topic.getStatus() != 1) { // 1: 已发布
			throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND_OR_NOT_PUBLISHED);
		}

		// 检查是否已绑定
		LambdaQueryWrapper<QuoteTopic> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(QuoteTopic::getQuoteId, dto.getQuoteId())
				.eq(QuoteTopic::getTopicId, dto.getTopicId())
				.eq(QuoteTopic::getDeleted, 0);
		QuoteTopic existing = this.getOne(wrapper);
		if (existing != null) {
			throw new BusinessException(ErrorCode.QUOTE_ALREADY_BOUND_TO_TOPIC);
		}

		// 创建绑定关系
		QuoteTopic quoteTopic = new QuoteTopic();
		quoteTopic.setQuoteId(dto.getQuoteId());
		quoteTopic.setTopicId(dto.getTopicId());
		quoteTopic.setBinderId(dto.getBinderId());
		quoteTopic.setBindTime(LocalDateTime.now());
		this.save(quoteTopic);

		// 更新统计信息
		// topicStatisticsService.incrementQuoteCount(dto.getTopicId(), dto.getBinderId());
	}

	@Override
	public Page<TopicQuoteVO> queryTopicQuotes(TopicQuoteQueryDTO queryDTO) {
		Long topicId = queryDTO.getTopicId();
		Integer page = queryDTO.getPage();
		Integer size = queryDTO.getSize();
		Page<QuoteTopic> voPage = new Page<>(page, size);
		LambdaQueryWrapper<QuoteTopic> wrapper = new LambdaQueryWrapper<QuoteTopic>()
				.eq(QuoteTopic::getTopicId, topicId)
				.eq(QuoteTopic::getDeleted, 0);
		Page<QuoteTopic> quoteTopicPage = this.page(voPage, wrapper);
		List<QuoteTopic> records = quoteTopicPage.getRecords();
		if (DoubaoUtils.isEmpty(records)) {
			return new Page<>();
		}
		List<Long> quoteIds = records.stream().map(QuoteTopic::getQuoteId).collect(Collectors.toList());
		List<Map<String, Object>> data = quoteClient.topicBatch(quoteIds).getData();
		if (DoubaoUtils.isEmpty(data)) {
			return new Page<>();
		}
		List<TopicQuoteVO> topicQuoteVOS = data.stream().map(item -> {
			TopicQuoteVO topicQuoteVO = new TopicQuoteVO();
			topicQuoteVO.setId(Long.parseLong(item.get("id").toString()));
			topicQuoteVO.setContent(item.get("content").toString());
			topicQuoteVO.setAuthor(item.get("author").toString());
			topicQuoteVO.setSource(item.get("source").toString());
			topicQuoteVO.setOriginal(Integer.parseInt(item.get("original").toString()));
			topicQuoteVO.setUserInfo((UserInfoDes) item.get("userInfo"));
			topicQuoteVO.setCreatedTime(LocalDateTime.parse(item.get("createdTime").toString()));
			return topicQuoteVO;
		}).collect(Collectors.toList());
		Page<TopicQuoteVO> pageVO = new Page<>();
		pageVO.setCurrent(page);
		pageVO.setSize(size);
		pageVO.setTotal(quoteTopicPage.getTotal());
		pageVO.setRecords(topicQuoteVOS);
		return pageVO;
	}
}
