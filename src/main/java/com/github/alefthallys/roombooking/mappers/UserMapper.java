package com.github.alefthallys.roombooking.mappers;

import com.github.alefthallys.roombooking.dtos.UserDTO;
import com.github.alefthallys.roombooking.models.User;

public class UserMapper {
	
	public static UserDTO toDto(User user) {
		return new UserDTO(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getPassword(),
				user.getPhone(),
				user.getRole()
		);
	}
	
	public static User toEntity(UserDTO userDTO) {
		return new User(
				userDTO.id(),
				userDTO.name(),
				userDTO.email(),
				userDTO.password(),
				userDTO.phone(),
				userDTO.role()
		);
	}
}
