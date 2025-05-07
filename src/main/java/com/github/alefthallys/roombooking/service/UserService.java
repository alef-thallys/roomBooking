package com.github.alefthallys.roombooking.service;

import com.github.alefthallys.roombooking.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	
	public String findAll() {
		return "List of users";
	}
	
	public String findById(Long id) {
		return "User by id";
	}
	
	public String create(User user) {
		return "User created";
	}
	
	public String update(Long id, User user) {
		return "User updated";
	}
	
	public String delete(Long id) {
		return "User deleted";
	}
}
