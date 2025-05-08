package com.github.alefthallys.roombooking.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRequestDTO(
		@Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
		@NotBlank(message = "Name is required")
		String name,
		
		@Email(message = "Email must be valid")
		@Size(max = 50, message = "Email must be less than 50 characters")
		@NotBlank(message = "Email is required")
		String email,
		
		@Size(min = 8, message = "Password must be at least 8 characters")
		@NotBlank(message = "Password is required")
		String password,
		
		@NotBlank(message = "Phone is required")
		String phone
) {
}
