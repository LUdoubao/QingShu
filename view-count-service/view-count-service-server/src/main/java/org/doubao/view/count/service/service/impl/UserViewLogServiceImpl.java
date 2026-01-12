package org.doubao.view.count.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.view.count.service.entity.UserViewLog;
import org.doubao.view.count.service.mapper.UserViewLogMapper;
import org.doubao.view.count.service.service.UserViewLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class UserViewLogServiceImpl extends ServiceImpl<UserViewLogMapper, UserViewLog> implements UserViewLogService {
	@Resource
	private UserViewLogMapper userViewLogMapper;
	@Override
	public List<Map<String, Object>> selectDailyViewCounts(List<Long> contentIds, List<LocalDate> dates) {
		return userViewLogMapper.selectDailyViewCounts(contentIds, dates);
	}
}
