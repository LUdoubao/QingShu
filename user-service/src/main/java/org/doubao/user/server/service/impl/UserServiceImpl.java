package org.doubao.user.server.service.impl;

import org.doubao.user.server.entity.User;
import org.doubao.user.server.mapper.UserMapper;
import org.doubao.user.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserMapper userMapper;

	@Override
	public List<User> listAll() {
		return userMapper.selectList(null);
	}

	@Override
	public User getById(Long id) {
		return userMapper.selectById(id);
	}

	@Override
	public void save(User user) {
		userMapper.insert(user);
	}
}
