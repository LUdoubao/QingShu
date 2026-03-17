package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.dto.TopicContentDto;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.TopicQuoteVO;
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
import org.doubao.topic.service.service.TopicStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuoteTopicServiceImpl extends ServiceImpl<QuoteTopicMapper, QuoteTopic> implements QuoteTopicService {
	@Resource
	private TopicMapper topicMapper;
	@Resource
	private QuoteClient quoteClient;
	@Resource
	private TopicStatisticsService topicStatisticsService;
	private static final Logger logger = LoggerFactory.getLogger(QuoteTopicServiceImpl.class);
	@Override
	public void deleteQuoteBind(List<Long> quoteIds) {
		if (quoteIds == null || quoteIds.isEmpty()) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		LambdaQueryWrapper<QuoteTopic> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(QuoteTopic::getQuoteId, quoteIds);
		List<QuoteTopic> list = this.list(wrapper);
		if (DoubaoUtils.isEmpty(list)) {
			return;
		}
		Set<Long> topicIds = list.stream().map(QuoteTopic::getTopicId).collect(Collectors.toSet());
		this.remove(new LambdaQueryWrapper<QuoteTopic>()
				.in(QuoteTopic::getQuoteId, quoteIds));

		topicStatisticsService.decrementQuoteCount(topicIds);
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
				.eq(QuoteTopic::getTopicId, dto.getTopicId());
		QuoteTopic existing = this.getOne(wrapper);
		if (existing != null) {
			Integer status = existing.getStatus();
			// 更新
			existing.setStatus(dto.getStatus());
			existing.setBindTime(LocalDateTime.now());
			this.updateById(existing);
			if (!Objects.equals(dto.getStatus(), status)) {
				logger.info("更新话题引用关系，话题id:{} , 更新状态:{}", dto.getTopicId(), dto.getStatus());
				if (dto.getStatus() == 1) {
					// 增加话题引用计数
					topicStatisticsService.incrementQuoteCount(dto.getTopicId());
				} else {
					// 减少话题引用计数
					topicStatisticsService.decrementQuoteCount(dto.getTopicId());
				}
			}

			return;
		}

		// 创建绑定关系
		QuoteTopic quoteTopic = new QuoteTopic();
		quoteTopic.setQuoteId(dto.getQuoteId());
		quoteTopic.setTopicId(dto.getTopicId());
		quoteTopic.setBinderId(dto.getBinderId());
		quoteTopic.setBindTime(LocalDateTime.now());
		quoteTopic.setStatus(dto.getStatus());
		this.save(quoteTopic);

		// 增加话题引用计数
		topicStatisticsService.incrementQuoteCount(dto.getTopicId());
	}

	@Override
	public void updateQuoteBind(TopicBindDTO dto) {
		LambdaQueryWrapper<QuoteTopic> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(QuoteTopic::getQuoteId, dto.getQuoteId());
		QuoteTopic quoteTopic = this.getOne(wrapper);
		if (DoubaoUtils.isNotEmpty(quoteTopic)) {
			Integer status = quoteTopic.getStatus();

			quoteTopic.setStatus(dto.getStatus());
			quoteTopic.setBindTime(LocalDateTime.now());
			this.updateById(quoteTopic);
			if (!Objects.equals(dto.getStatus(), status)) {
				if (dto.getStatus() == 1) {
					// 添加话题引用计数
					topicStatisticsService.incrementQuoteCount(quoteTopic.getTopicId());
				} else {
					// 减少话题引用计数
					topicStatisticsService.decrementQuoteCount(quoteTopic.getTopicId());
				}
			}
		}
	}

	@Override
	public Page<TopicQuoteVO> queryTopicQuotes(TopicQuoteQueryDTO queryDTO) {
		Long topicId = queryDTO.getTopicId();
		Integer page = queryDTO.getPage();
		Integer size = queryDTO.getSize();
		Page<QuoteTopic> voPage = new Page<>(page, size);
		LambdaQueryWrapper<QuoteTopic> wrapper = new LambdaQueryWrapper<QuoteTopic>()
				.eq(QuoteTopic::getTopicId, topicId);
		Page<QuoteTopic> quoteTopicPage = this.page(voPage, wrapper);
		List<QuoteTopic> records = quoteTopicPage.getRecords();
		if (DoubaoUtils.isEmpty(records)) {
			return new Page<>();
		}
		List<Long> quoteIds = records.stream().map(QuoteTopic::getQuoteId).collect(Collectors.toList());
		Long userId = UserContext.getUserId();
		TopicContentDto topicContentDto = new TopicContentDto();
		topicContentDto.setContentIds(quoteIds);
		topicContentDto.setCurrentUserId(userId);
		List<Map<String, Object>> data = quoteClient.topicBatch(topicContentDto).getData();
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
			topicQuoteVO.setFollow(item.get("follow").toString().equals("true"));
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
