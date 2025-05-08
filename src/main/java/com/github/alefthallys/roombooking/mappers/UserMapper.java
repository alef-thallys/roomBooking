package com.github.alefthallys.roombooking.mappers;

import com.github.alefthallys.roombooking.dtos.UserRequestDTO;
import com.github.alefthallys.roombooking.dtos.UserResponseDTO;
import com.github.alefthallys.roombooking.models.User;

public class UserMapper {
	
	public static UserResponseDTO toDto(User user) {
		return new UserResponseDTO(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getPhone(),
				user.getRole()
		);
	}
	
	public static User toEntity(UserRequestDTO userDTO) {
		return new User(
				userDTO.name(),
				userDTO.email(),
				userDTO.password(),
				userDTO.phone()
		);
	}
}
