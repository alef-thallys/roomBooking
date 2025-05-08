package com.github.alefthallys.roombooking.dtos;

import com.github.alefthallys.roombooking.models.User;

public record UserDTO(
		Long id,
		String name,
		String email,
		String password,
		String phone,
		User.Role role
) {
}
