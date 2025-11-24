package org.doubao.quote.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.*;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.vo.*;

import java.util.List;
import java.util.Map;

/**
 * 引文服务接口
 * 提供引文的增删改查、审核、统计等相关业务功能
 */
public interface QuoteService extends IService<Quote> {

	/**
	 * 分页查询引文列表
	 * @param pageDto 分页查询参数
	 * @return 包含引文分页数据的响应结果
	 */
	Result<Page<QuoteVo>> page(PageDto pageDto);

	/**
	 * 新增引文
	 * @param dto 引文数据传输对象
	 * @return 操作结果
	 */
	Result<Void> addQuote(QuoteDTO dto);

	/**
	 * 批量删除引文
	 * @param quoteIds 引文ID列表
	 * @return 包含操作结果的响应
	 */
	Result<String> deleteQuote(List<Long> quoteIds);

	/**
	 * 更新引文信息
	 * @param dto 引文更新数据传输对象
	 * @return 包含操作结果的响应
	 */
	Result<String> updateQuote(QuoteUpdateDto dto);

	/**
	 * 根据ID获取引文详情
	 * @param id 引文ID
	 * @return 包含引文详情的响应结果
	 */
	Result<QuoteVo> getDetailById(Long id);

	/**
	 * 公开接口-根据ID和状态列表获取引文详情
	 * @param id 引文ID
	 * @param statusList 状态列表（用于过滤）
	 * @return 包含引文详情的响应结果
	 */
	Result<QuoteVo> publicGetDetailById(Long id, List<Integer> statusList);

	/**
	 * 管理员分页查询引文列表
	 * @param pageDto 分页查询参数
	 * @return 包含引文分页数据的响应结果（管理员视图）
	 */
	Result<Page<QuoteVo>> pageManager(PageDto pageDto);

	/**
	 * 验证引文数据
	 * @param dto 引文数据传输对象
	 * @return 验证结果
	 */
	Result<String> verify(QuoteDTO dto);

	/**
	 * 获取待审核引文的详情
	 * @param id 引文ID
	 * @return 包含引文详情的响应结果（审核视图）
	 */
	Result<QuoteVo> getVerifyDetailById(Long id);

	/**
	 * 分页查询待审核引文列表
	 * @param pageDto 分页查询参数
	 * @return 包含待审核引文分页数据的响应结果
	 */
	Result<Page<QuoteVo>> verifyPage(PageDto pageDto);

	/**
	 * 批量操作引文
	 * @param ids 引文ID列表
	 * @return 批量操作结果列表
	 */
	Result<List<Map<String, Object>>> batch(List<Long> ids);

	/**
	 * 根据引文ID获取引文类型
	 * @param quoteId 引文ID
	 * @return 引文类型
	 */
	String getQuoteType(String quoteId);

	/**
	 * 检查引文是否存在
	 * @param request 检查请求参数
	 * @return 是否存在
	 */
	boolean checkQuoteExists(Map<String, String> request);

	/**
	 * 原创数据分页查询
	 * @param pageDto 分页查询参数
	 */
	Result<Page<QuoteVo>> originalPage(PageDto pageDto);

	/**
	 * 获取搜索建议
	 * @param keyword 搜索关键词
	 * @return 搜索建议结果
	 */
	Result<Map<String, String>> getSearchSuggestions(String keyword);

	/**
	 * 引文搜索
	 * @param keyword 搜索关键词
	 * @param page 页码
	 * @param size 每页大小
	 * @param type 搜索类型
	 * @param currentUserId 当前用户ID
	 * @return 搜索结果
	 */
	Result<Map<String, Object>> search(String keyword, int page, int size, String type, Long currentUserId);

	/**
	 * 更新引文状态
	 * @param request 状态更新请求参数
	 */
	void updateStatus(Map<String, String> request);

	/**
	 * 查询引文数据
	 * @param queryDataPageDto 引文数据查询参数
	 * @return 引文数据分页结果
	 */
	Page<QuoteDataVo> queryQuoteData(QueryDataPageDto queryDataPageDto);

	/**
	 * 查询引文状态统计
	 * @return 引文状态统计视图对象
	 */
	QuoteStatusCountVo queryStatusCount();

	/**
	 * 查询内容概览统计
	 * @return 内容概览视图对象
	 */
	ContentOverviewVo queryContentOverview();

	/**
	 * 查询内容趋势统计
	 * @param days 统计天数
	 * @param metrics 统计指标列表
	 * @return 内容趋势视图对象列表
	 */
	List<ContentTrendVo> queryContentTrend(int days, List<String> metrics);

	/**
	 * 获取引文更新详情
	 * @param id 引文ID
	 * @return 引文详情视图对象（用于更新操作）
	 */
	QuoteVo getUpdateDetail(Long id);

	/**
	 * 更新引文状态
	 * @param quoteId 引文ID
	 * @param status 引文状态
	 */
	void updateQuoteStatus(Long quoteId, Integer status);

	/**
	 * 保存为草稿
	 */
	Long saveAsDraft(QuoteDTO dto);
}
