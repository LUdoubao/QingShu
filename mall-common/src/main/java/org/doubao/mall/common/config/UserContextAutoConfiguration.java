package org.doubao.mall.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.doubao.mall.common.handler.MyMetaObjectHandler;
import org.doubao.mall.common.handler.UserContextFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserContextAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public UserContextFilter userContextFilter() {
		return new UserContextFilter();
	}

	@Bean
	public MyMetaObjectHandler myMetaObjectHandler() {
		return new MyMetaObjectHandler();
	}

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 添加分页插件
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
	}
}