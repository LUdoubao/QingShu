package org.doubao.view.count.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.view.count.service.entity.UserViewLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserViewLogService extends IService<UserViewLog> {
	List<Map<String, Object>> selectDailyViewCounts(List<Long> contentIds, List<LocalDate> dates);
}
