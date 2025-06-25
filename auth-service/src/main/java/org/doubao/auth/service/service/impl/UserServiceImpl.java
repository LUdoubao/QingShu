package org.doubao.auth.service.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.auth.service.entity.User;
import org.doubao.auth.service.mapper.UserMapper;
import org.doubao.auth.service.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
	@Override
	public User getByUsername(String username) {
		return lambdaQuery().eq(User::getUsername, username).one();
	}
}
