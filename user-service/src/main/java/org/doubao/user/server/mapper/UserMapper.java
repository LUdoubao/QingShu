package org.doubao.user.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
	User findByEmail(@Param("email") String email);

	void updateUserAvatar(@Param("userId") Long userId,
						  @Param("fileKey") String fileKey,
						  @Param("storageType") String storageType);

	boolean findByUsername(@Param("username") String username);
}