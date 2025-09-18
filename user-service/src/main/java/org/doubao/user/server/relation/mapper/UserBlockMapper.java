package org.doubao.user.server.relation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.relation.entity.UserBlock;
import org.doubao.user.server.relation.vo.BlockCheckVo;
import org.doubao.user.server.relation.vo.BlockedUserVO;

import java.util.List;
import java.util.Map;

/**
 * 黑名单Mapper接口
 */
public interface UserBlockMapper extends BaseMapper<UserBlock> {
	/**
	 * 检查是否存在拉黑关系
	 */
	Integer checkBlockRelation(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);

	/**
	 * 获取黑名单用户列表
	 */
	List<BlockedUserVO> selectBlockedUserList(@Param("userId") Long userId,
											   @Param("offset") Integer offset, @Param("size") Integer size);

	Integer getBlockCount(@Param("userId") Long userId);

	List<BlockCheckVo> checkBatch(@Param("userId")  Long userId, @Param("targetUserIds") List<Long> targetUserIds);
}