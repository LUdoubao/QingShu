package org.doubao.feed.service.service;

import org.doubao.feed.service.feign.QuoteClient;
import org.doubao.feed.service.model.dto.DynamicDTO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class SortingService {

	private QuoteClient quoteClient;

	/**
	 * 按指定方式排序动态列表
	 */
	public List<DynamicDTO> sortDynamics(List<DynamicDTO> dynamics, int sortType, Long currentUserId) {
		if (sortType == 0) {
			// 时间倒序
			return sortByTime(dynamics);
		} else {
			// 智能权重排序
			return sortByWeight(dynamics, currentUserId);
		}
	}

	/**
	 * 按时间倒序排序
	 */
	private List<DynamicDTO> sortByTime(List<DynamicDTO> dynamics) {
		dynamics.sort(Comparator.comparing(DynamicDTO::getEventTime).reversed());
		return dynamics;
	}

	/**
	 * 按智能权重排序
	 */
	private List<DynamicDTO> sortByWeight(List<DynamicDTO> dynamics, Long currentUserId) {
		// 计算每条动态的权重
		dynamics.forEach(dynamic -> {
			double weight = calculateDynamicWeight(dynamic, currentUserId);
			dynamic.setWeight(weight);
		});

		// 按权重降序排序，权重相同则按时间降序
		dynamics.sort(Comparator.comparing(DynamicDTO::getWeight).reversed()
				.thenComparing(DynamicDTO::getEventTime).reversed());

		return dynamics;
	}

	/**
	 * 计算动态权重
	 */
	private double calculateDynamicWeight(DynamicDTO dynamic, Long currentUserId) {
		// 1. 时间衰减因子
		double timeFactor = calculateTimeFactor(dynamic.getEventTime());

		// 2. 亲密度因子
		double intimacyFactor = calculateIntimacyFactor(currentUserId, dynamic.getActorId());

		// 3. 热度因子
		double hotFactor = calculateHotFactor(dynamic.getTargetId(), dynamic.getTargetType());

		// 4. 兴趣匹配度因子
		double interestFactor = calculateInterestFactor(currentUserId, dynamic.getTargetId());

		// 最终权重计算
		return 1.0 * timeFactor * (1 + intimacyFactor) * (1 + hotFactor) * (1 + interestFactor);
	}

	/**
	 * 计算时间衰减因子
	 */
	private double calculateTimeFactor(LocalDateTime eventTime) {
		Duration duration = Duration.between(eventTime, LocalDateTime.now());
		long hours = duration.toHours();

		if (hours < 1) {
			return 1.5; // 1小时内
		} else if (hours < 24) {
			return 1.2; // 1-24小时
		} else if (hours < 72) {
			return 1.0; // 24-72小时
		} else if (hours < 168) {
			return 0.8; // 3-7天
		} else {
			return 0.5; // 7天以上
		}
	}

	/**
	 * 计算亲密度因子
	 */
	private double calculateIntimacyFactor(Long userId, Long actorId) {
		return 0.5;
	}

	/**
	 * 计算热度因子
	 */
	private double calculateHotFactor(Long targetId, String targetType) {
		return 0;
	}

	/**
	 * 计算兴趣匹配度因子
	 */
	private double calculateInterestFactor(Long userId, Long targetId) {
		return 0;
	}
}