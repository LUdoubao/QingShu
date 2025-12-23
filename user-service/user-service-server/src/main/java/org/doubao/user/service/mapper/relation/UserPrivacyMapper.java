package org.doubao.user.service.mapper.relation;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.service.entity.relation.UserPrivacy;

/**
 * 用户隐私设置Mapper接口
 */
@Mapper
public interface UserPrivacyMapper extends BaseMapper<UserPrivacy> {

	/**
	 * 根据用户ID查询隐私设置
	 * @param userId 用户ID
	 */
	UserPrivacy selectByUserId(@Param("userId") Long userId);

}
