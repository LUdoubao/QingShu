package org.doubao.mall.common.condition;

import org.springframework.context.annotation.Conditional;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(MicroserviceModeCondition.class)
public @interface MicroserviceMode {
}
