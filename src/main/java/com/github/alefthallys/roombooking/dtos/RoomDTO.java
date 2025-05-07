package com.github.alefthallys.roombooking.dtos;

public record RoomDTO(
		Long id,
		String name,
		String description,
		int capacity,
		boolean available,
		String location
) {
}
