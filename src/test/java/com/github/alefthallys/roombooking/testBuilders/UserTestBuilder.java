package com.github.alefthallys.roombooking.testBuilders;

import com.github.alefthallys.roombooking.dtos.Auth.LoginRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.User.UserResponseDTO;
import com.github.alefthallys.roombooking.dtos.User.UserUpdateRequestDTO;
import com.github.alefthallys.roombooking.models.User;
import org.springframework.security.core.userdetails.UserDetails;

public class UserTestBuilder {
	
	private Long id = 1L;
	private String name = "John Doe";
	private String email = "john@gmail.com";
	private String password = "password";
	private String phone = "1299994444";
	private User.Role role = User.Role.USER;
	
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
	
	public UserDetails buildUserDetails() {
		return org.springframework.security.core.userdetails.User.builder()
				.username(email)
				.password(password)
				.roles(role.name())
				.build();
	}
}