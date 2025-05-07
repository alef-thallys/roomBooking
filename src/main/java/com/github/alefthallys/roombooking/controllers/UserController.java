package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.dtos.UserDTO;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	public List<UserDTO> findAll() {
		return userService.findAll();
	}
	
	@GetMapping("/{id}")
	public UserDTO findById(@PathVariable Long id) {
		return userService.findById(id);
	}
	
	@PostMapping
	public UserDTO create(@RequestBody UserDTO userDTO) {
		return userService.create(userDTO);
	}
	
	@PutMapping("/{id}")
	public UserDTO update(@PathVariable Long id, @RequestBody UserDTO userDTO) {
		return userService.update(id, userDTO);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		userService.delete(id);
	}
}
