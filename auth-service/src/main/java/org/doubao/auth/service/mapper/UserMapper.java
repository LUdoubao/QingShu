package org.doubao.auth.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.auth.service.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
