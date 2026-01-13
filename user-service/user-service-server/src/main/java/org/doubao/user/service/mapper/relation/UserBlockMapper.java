package org.doubao.user.service.mapper.relation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.service.entity.relation.UserBlock;
import org.doubao.user.service.vo.relation.BlockCheckVo;
import org.doubao.user.service.vo.relation.BlockedUserVO;

import java.util.List;
import java.util.Map;

/**
 * 黑名单Mapper接口
 */
@Mapper
public interface UserBlockMapper extends BaseMapper<UserBlock> {

	Integer checkBlockRelation(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);


	List<BlockedUserVO> selectBlockedUserList(@Param("userId") Long userId,
											  @Param("offset") Integer offset, @Param("size") Integer size);

	Integer getBlockCount(@Param("userId") Long userId);

	List<BlockCheckVo> checkBatch(@Param("userId")  Long userId, @Param("targetUserIds") List<Long> targetUserIds);
}
