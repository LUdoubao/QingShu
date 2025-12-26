package org.doubao.dialog.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.dialog.service.entity.AssistantDialog;
import org.doubao.dialog.service.vo.DialogQuery;
import org.doubao.dialog.service.vo.DialogVO;

/**
 * 对话Mapper接口
 */
@Mapper
public interface AssistantDialogMapper extends BaseMapper<AssistantDialog> {

	/**
	 * 根据用户ID查询对话列表
	 */
	IPage<DialogVO> selectByUserId(Page<DialogVO> page, @Param("userId") Long userId);

	/**
	 * 管理员查询对话列表
	 */
	IPage<DialogVO> selectByAdminQuery(Page<DialogVO> page, @Param("query") DialogQuery query);

	/**
	 * 根据对话ID查询用户ID
	 */
	Long selectUserIdByDialogId(@Param("dialogId") Long dialogId);
}