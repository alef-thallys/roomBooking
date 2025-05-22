package com.github.alefthallys.roombooking.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoomResponseDTO(
		Long id,
		String name,
		String description,
		int capacity,
		boolean available,
		String location
) {
}
