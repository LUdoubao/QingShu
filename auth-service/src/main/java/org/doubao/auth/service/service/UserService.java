package org.doubao.auth.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.auth.service.entity.User;

public interface UserService extends IService<User> {
	User getByUsername(String username);
}
