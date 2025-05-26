package com.github.alefthallys.roombooking.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservationResponseDTO(
		Long id,
		LocalDateTime startDate,
		LocalDateTime endDate,
		UserResponseDTO user,
		RoomResponseDTO room
) {
}
