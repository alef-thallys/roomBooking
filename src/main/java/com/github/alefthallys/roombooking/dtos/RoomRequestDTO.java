package com.github.alefthallys.roombooking.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoomRequestDTO(
		
		@Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
		@NotBlank(message = "Name is required")
		String name,
		
		@Size(max = 555, message = "Description must be less than 555 characters")
		@NotBlank(message = "Description is required")
		String description,
		
		@Min(value = 2, message = "Capacity must be at least 2")
		@NotNull(message = "Capacity is required")
		int capacity,
		
		@NotNull(message = "Available status is required")
		boolean available,
		
		@Size(max = 50, message = "Location must be less than 50 characters")
		@NotBlank(message = "Location is required")
		String location
) {
}
