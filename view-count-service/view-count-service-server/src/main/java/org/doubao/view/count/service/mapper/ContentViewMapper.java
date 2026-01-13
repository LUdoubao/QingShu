package org.doubao.view.count.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.doubao.view.count.service.entity.ContentView;

import java.util.List;

public interface ContentViewMapper extends BaseMapper<ContentView> {
	void insertBatchSomeColumn(List<ContentView> insertList);
}