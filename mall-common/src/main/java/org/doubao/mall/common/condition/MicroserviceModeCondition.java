package org.doubao.mall.common.condition;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class MicroserviceModeCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		Environment env = context.getEnvironment();
		String runMode = env.getProperty("service.run-mode", "monolith");
		return "microservice".equalsIgnoreCase(runMode);
	}
}

