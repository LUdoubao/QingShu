package org.doubao.user.server.controller;

import org.doubao.user.server.entity.User;
import org.doubao.user.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping
	public List<User> list() {
		return userService.listAll();
	}

	@GetMapping("/{id}")
	public User get(@PathVariable Long id) {
		return userService.getById(id);
	}

	@PostMapping
	public void save(@RequestBody User user) {
		userService.save(user);
	}
}