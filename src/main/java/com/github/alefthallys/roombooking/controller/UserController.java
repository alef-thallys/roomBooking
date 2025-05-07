package com.github.alefthallys.roombooking.controller;

import com.github.alefthallys.roombooking.model.User;
import com.github.alefthallys.roombooking.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	public String findAll() {
		return userService.findAll();
	}
	
	@GetMapping("/{id}")
	public String findById(@PathVariable Long id) {
		return userService.findById(id);
	}
	
	@PostMapping
	public String create(@RequestBody User user) {
		return userService.create(user);
	}
	
	@PutMapping("/{id}")
	public String update(@PathVariable Long id, @RequestBody User user) {
		return userService.update(id, user);
	}
	
	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
		return userService.delete(id);
	}
}
