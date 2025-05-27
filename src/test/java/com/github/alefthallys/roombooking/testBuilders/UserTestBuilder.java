package com.github.alefthallys.roombooking.testBuilders;

import com.github.alefthallys.roombooking.dtos.LoginRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.models.User;

public class UserTestBuilder {
	
	private Long id = 1L;
	private String name = "John Doe";
	private String email = "john@gmail.com";
	private String password = "password";
	private String phone = "123456789";
	private User.Role role = User.Role.ROLE_USER;
	
	public static UserTestBuilder anUser() {
		return new UserTestBuilder();
	}
	
	public UserTestBuilder withId(Long id) {
		this.id = id;
		return this;
	}
	
	public UserTestBuilder withName(String name) {
		this.name = name;
		return this;
	}
	
	public UserTestBuilder withEmail(String email) {
		this.email = email;
		return this;
	}
	
	public UserTestBuilder withPassword(String password) {
		this.password = password;
		return this;
	}
	
	public UserTestBuilder withPhone(String phone) {
		this.phone = phone;
		return this;
	}
	
	public UserTestBuilder withRole(User.Role role) {
		this.role = role;
		return this;
	}
	
	public User build() {
		User user = new User();
		user.setId(id);
		user.setName(name);
		user.setEmail(email);
		user.setPassword(password);
		user.setPhone(phone);
		user.setRole(role);
		return user;
	}
	
	public UserRequestDTO buildRequestDTO() {
		return new UserRequestDTO(name, email, password, phone);
	}
	
	public UserUpdateRequestDTO buildUpdateRequestDTO() {
		return new UserUpdateRequestDTO(name, password, phone);
	}
	
	public UserResponseDTO buildResponseDTO() {
		return new UserResponseDTO(id, name, email, phone, role);
	}
	
	public LoginRequestDTO buildLoginRequestDTO() {
		return new LoginRequestDTO(email, password);
	}
}