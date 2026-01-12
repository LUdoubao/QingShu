package org.doubao.view.count.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.view.count.service.entity.ViewCorrectionLog;
import org.doubao.view.count.service.mapper.ViewCorrectionLogMapper;
import org.doubao.view.count.service.service.ViewCorrectionLogService;
import org.springframework.stereotype.Service;

@Service
public class ViewCorrectionLogServiceImpl extends ServiceImpl<ViewCorrectionLogMapper, ViewCorrectionLog>
		implements ViewCorrectionLogService {
}
