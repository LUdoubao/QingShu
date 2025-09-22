package org.doubao.mall.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.UserLoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
	private static final Logger log = LoggerFactory.getLogger(MyMetaObjectHandler.class);
	@Override
	public void insertFill(MetaObject metaObject) {
		log.info("开始插入填充...");
		// 自动填充时间
		this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
		this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());

		// 自动填充用户 ID（当前登录用户）从 ThreadLocal 中取
		Long userId = getCurrentUserId(); // ↓ 下面提供方法实现
		log.info("当前用户ID：{}", userId);
		this.strictInsertFill(metaObject, "createdId", Long.class, userId);
		this.strictInsertFill(metaObject, "updatedId", Long.class, userId);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		// 自动更新 updateTime 和 updateId
		this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
		this.strictUpdateFill(metaObject, "updatedId", Long.class, getCurrentUserId());
	}

	private Long getCurrentUserId() {
		UserLoginVo user = UserContext.getUser();
		return user != null ? user.getId() : null;
	}
}
