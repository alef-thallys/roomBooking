package com.github.alefthallys.roombooking.dtos.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
		@Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
		String name,
		
		@Size(min = 8, message = "Password must be at least 8 characters")
		String password,
		
		@NotBlank(message = "Phone is required")
		String phone
) {
}
