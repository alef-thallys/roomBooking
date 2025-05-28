package com.github.alefthallys.roombooking.dtos.User;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.alefthallys.roombooking.models.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponseDTO(
		Long id,
		String name,
		String email,
		String phone,
		User.Role role
) {
}
