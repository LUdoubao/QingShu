package org.doubao.user.service.mapper.core;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.service.entity.core.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
	User findByEmail(@Param("email") String email);

	void updateUserAvatar(@Param("userId") Long userId,
						  @Param("fileKey") String fileKey,
						  @Param("storageType") String storageType);

	boolean findByUsername(@Param("username") String username);

	Integer existsByIdAndStatus(@Param("userId") Long userId, @Param("status") int status);

	void updateUserBg(@Param("userId") Long userId,
					  @Param("fileKey") String fileKey,
					  @Param("storageType") String storageType);

	boolean existsByNickname(@Param("nickname") String nickname, @Param("userId") Long userId);

	void updateUserPassword(@Param("email") String email, @Param("pwd") String pwd);
}
