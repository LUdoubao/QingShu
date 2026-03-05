package org.doubao.mall.common.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MonolithModeCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Environment env = context.getEnvironment();
		String runMode = env.getProperty("service.run-mode", "monolith");
		return "monolith".equalsIgnoreCase(runMode);
	}
}
