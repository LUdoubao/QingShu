package org.doubao.dialog.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.dialog.service.entity.AssistantMessage;

import java.util.List;

/**
 * 消息Mapper接口
 * 业务说明：定义AI助手消息相关的数据库操作接口
 * 核心功能：1. 消息的增删改查 2. 对话消息查询 3. 消息搜索功能
 * 适用场景：
 * 1. AI助手对话消息存储
 * 2. 对话历史记录管理
 * 3. 消息搜索与检索
 */
@Mapper
public interface AssistantMessageMapper extends BaseMapper<AssistantMessage> {

	/**
	 * 根据对话ID查询最新N条消息
	 * 业务说明：查询指定对话的最新N条消息记录，用于获取对话历史
	 * 业务流程：根据对话ID查询数据库，按时间倒序排列，限制返回数量
	 * 参数校验：对话ID和限制数量不能为空
	 * 数据处理：按发送时间倒序排列，返回最新的limit条记录
	 * @param dialogId 对话ID，标识消息所属对话
	 * @param limit 限制返回数量，指定返回最新消息的数量
	 * @return 最新N条消息列表，按时间倒序排列
	 */
	List<AssistantMessage> selectLastNByDialogId(
			@Param("dialogId") Long dialogId,
			@Param("limit") Integer limit);

	/**
	 * 根据对话ID查询所有消息
	 * 业务说明：查询指定对话的所有消息记录
	 * 业务流程：根据对话ID查询数据库中的所有相关消息
	 * 参数校验：对话ID不能为空
	 * 数据处理：按发送时间倒序排列所有消息
	 * @param dialogId 对话ID，标识消息所属对话
	 * @return 对话的所有消息列表，按时间倒序排列
	 */
	List<AssistantMessage> selectByDialogId(@Param("dialogId") Long dialogId);

	/**
	 * 查询对话的最后一条消息
	 * 业务说明：查询指定对话的最后一条消息，用于获取对话的最新状态
	 * 业务流程：根据对话ID查询数据库，按时间倒序排列后取第一条记录
	 * 参数校验：对话ID不能为空
	 * 数据处理：按发送时间倒序排列，返回时间最新的单条消息
	 * @param dialogId 对话ID，标识消息所属对话
	 * @return 对话的最后一条消息，若不存在返回null
	 */
	AssistantMessage selectLastByDialogId(@Param("dialogId") Long dialogId);

	/**
	 * 根据关键词搜索消息
	 * 业务说明：在消息内容中根据关键词搜索匹配的消息记录
	 * 业务流程：根据关键词和时间范围查询数据库中的消息
	 * 参数校验：关键词、开始时间和结束时间格式需符合要求
	 * 数据处理：支持按时间范围过滤搜索结果，返回匹配的消息列表
	 * @param keyword 关键词，用于在消息内容中搜索
	 * @param startTime 开始时间，搜索的时间范围起始点
	 * @param endTime 结束时间，搜索的时间范围结束点
	 * @return 匹配关键词的消息列表
	 */
	List<AssistantMessage> searchMessages(
			@Param("keyword") String keyword,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime);
}