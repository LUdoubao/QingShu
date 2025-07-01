// package org.doubao.api.gateway.config.sentinel;
//
// import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
// import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
// import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
// import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
// import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
// import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
// import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
// import com.alibaba.csp.sentinel.slots.block.RuleConstant;
// import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
// import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.InitializingBean;
// import org.springframework.context.annotation.Configuration;
//
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
//
// /**
//  * Sentinel 网关规则配置类
//  * <p>
//  * 用途：配置网关API分组和流控规则
//  * <p>
//  * 实现 InitializingBean 接口，确保在Spring容器初始化完成后加载Sentinel规则
//  */
// @Configuration
// public class SentinelGatewayConfig implements InitializingBean {
// 	// 使用SLF4J记录日志
// 	private final static Logger LOGGER = LoggerFactory.getLogger(SentinelGatewayConfig.class);
//
// 	/**
// 	 * Spring容器初始化完成后自动执行的初始化方法
// 	 * <p>
// 	 * 负责两方面的初始化：
// 	 * 1. 定义API分组（资源分组）
// 	 * 2. 配置网关流控规则
// 	 */
// 	@Override
// 	public void afterPropertiesSet() {
// 		// ============ 1. 定义API分组（PRODUCT_API 产品接口组） ============
// 		Set<ApiDefinition> definitions = new HashSet<>();
//
// 		// 创建API定义：用于匹配 /products/** 路径的请求
// 		ApiDefinition apiDefinition = new ApiDefinition("PRODUCT_API")
// 				.setPredicateItems(new HashSet<ApiPredicateItem>() {{
// 					add(new ApiPathPredicateItem()
// 							// 匹配所有以/products开头的URL
// 							.setPattern("/products/**")
// 							// 使用前缀匹配策略（还有精确匹配、正则匹配等）
// 							.setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
// 				}});
//
// 		definitions.add(apiDefinition);
// 		// 将API分组定义加载到Sentinel的管理器中
// 		GatewayApiDefinitionManager.loadApiDefinitions(definitions);
//
// 		// ============ 2. 配置限流规则 ============
// 		Set<GatewayFlowRule> flowRules = new HashSet<>();
//
// 		// 创建针对PRODUCT_API组的限流规则
// 		flowRules.add(new GatewayFlowRule("PRODUCT_API") // 绑定到API分组名
// 						.setCount(1)          // 阈值：1次/秒（每间隔秒数内允许的请求数）
// 						.setIntervalSec(1)     // 统计时间窗口：1秒（默认值）
// 						.setBurst(5)           // 突发流量上限（令牌桶容量）
// 						.setGrade(RuleConstant.FLOW_GRADE_QPS)   // 限流类型（QPS/线程数）
// 						.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT) // 流控策略（直接拒绝/WarmUp/排队等待）
// 		);
//
// 		// 加载规则到Sentinel规则管理器
// 		GatewayRuleManager.loadRules(flowRules);
//
// 		// ==================== 3.熔断规则配置 ====================
// 		List<DegradeRule> degradeRules = new ArrayList<>();
//
// 		// 产品接口熔断规则（基于异常比例）
// 		degradeRules.add(new DegradeRule("PRODUCT_API")
// 				.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO) // 熔断策略：异常比例
// 				.setCount(0.3)                // 30%异常率（降低敏感度）
// 				.setMinRequestAmount(3)       // 最小3个请求即可统计
// 				.setStatIntervalMs(10000)     // 10秒统计窗口
// 				.setTimeWindow(60));
//
// 		DegradeRuleManager.loadRules(degradeRules);
//
// 		// 初始化完成日志
// 		LOGGER.info("✅ Sentinel 规则初始化完成: \n流量控制: {}\n熔断降级: {}\nAPI定义: {}",
// 				flowRules, degradeRules, definitions);
// 	}
// }
