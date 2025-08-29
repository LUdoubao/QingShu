package org.doubao.fanout.service.service;

import org.doubao.mall.common.enums.EventType;
import org.doubao.fanout.service.model.FanoutRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConfigurationProperties(prefix = "fanout")
public class RuleService {

	private static final Logger log = LoggerFactory.getLogger(RuleService.class);
	private List<FanoutRule> rules;

	// 事件类型到分发规则的映射
	private final Map<EventType, FanoutRule> ruleMap = new HashMap<>();

	/**
	 * 初始化时加载分发规则
	 */
	@PostConstruct
	public void loadRules() {
		try {
			if (rules != null) {
				for (FanoutRule rule : rules) {
					ruleMap.put(rule.getMessageType(), rule);
					log.info("Loaded fanout rule for event type: {}", rule.getMessageType());
				}
			}
			log.info("Successfully loaded {} fanout rules", ruleMap.size());
		} catch (Exception e) {
			log.error("Failed to load fanout rules", e);
			throw new RuntimeException("Failed to initialize fanout rules", e);
		}
	}

	/**
	 * 根据事件类型获取分发规则
	 */
	public FanoutRule getRuleByEventType(EventType eventType) {
		return ruleMap.get(eventType);
	}

	public List<FanoutRule> getRules() {
		return rules;
	}

	public void setRules(List<FanoutRule> rules) {
		this.rules = rules;
	}
}

