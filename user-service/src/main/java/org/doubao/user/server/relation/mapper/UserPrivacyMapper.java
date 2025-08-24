package org.doubao.user.server.relation.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.relation.entity.UserPrivacy;

/**
 * 用户隐私设置Mapper接口
 */
@Mapper
public interface UserPrivacyMapper extends BaseMapper<UserPrivacy> {

	/**
	 * 根据用户ID查询隐私设置
	 * @param userId 用户ID
	 * @return 隐私设置实体（无记录则返回null）
	 */
	UserPrivacy selectByUserId(@Param("userId") Long userId);

}
