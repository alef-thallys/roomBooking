package com.github.alefthallys.roombooking.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
		
		@NotBlank(message = "Username is required")
		String username,
		
		@NotBlank(message = "Password is required")
		String password
) {
}
