package org.doubao.view.count.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.view.count.service.dto.ViewRecordDTO;
import org.doubao.view.count.service.entity.ContentView;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ViewCountService extends IService<ContentView> {
	/**
	 * 记录浏览行为，返回是否有效计数
	 * @param record 浏览记录
	 * @return 是否有效计数
	 */
	boolean recordView(ViewRecordDTO record);

	/**
	 * 获取内容的浏览量
	 * @param contentId 内容ID
	 * @return 浏览量
	 */
	Long getViewCount(Long contentId);

	/**
	 * 批量获取内容的浏览量
	 * @param contentIds 内容ID列表
	 * @return 内容ID与浏览量的映射
	 */
	Map<Long, Long> batchGetViewCounts(List<Long> contentIds);

	/**
	 * 获取内容的浏览趋势
	 * @param contentId 内容ID
	 * @param days 天数
	 * @return 浏览趋势数据
	 */
	// List<ViewTrendDTO> getViewTrend(Long contentId, int days);

	/**
	 * 执行数据清洗与校正
	 */
	void cleanAndCorrectData();

	Map<LocalDate, Long> batchSumDailyCounts(Map<String, Object> params);
}
