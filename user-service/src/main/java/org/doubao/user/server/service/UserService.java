package org.doubao.user.server.service;

import org.doubao.user.server.entity.User;

import java.util.List;

public interface UserService {
	List<User> listAll();
	User getById(Long id);
	void save(User user);
}