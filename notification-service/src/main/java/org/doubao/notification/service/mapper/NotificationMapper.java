package org.doubao.notification.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.notification.service.entity.Notification;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
	int selectUnreadCount(@Param("userId") Long userId);

	int batchMarkAsRead(@Param("ids") List<Long> ids);
}