package org.doubao.dialog.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.dialog.service.entity.AssistantMessage;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface AssistantMessageMapper extends BaseMapper<AssistantMessage> {

	/**
	 * 根据对话ID查询最新N条消息
	 */
	List<AssistantMessage> selectLastNByDialogId(
			@Param("dialogId") Long dialogId,
			@Param("limit") Integer limit);

	/**
	 * 根据对话ID查询所有消息
	 */
	List<AssistantMessage> selectByDialogId(@Param("dialogId") Long dialogId);

	/**
	 * 查询对话的最后一条消息
	 */
	AssistantMessage selectLastByDialogId(@Param("dialogId") Long dialogId);

	/**
	 * 根据关键词搜索消息
	 */
	List<AssistantMessage> searchMessages(
			@Param("keyword") String keyword,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime);
}