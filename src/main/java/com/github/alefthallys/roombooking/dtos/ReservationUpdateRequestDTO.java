package com.github.alefthallys.roombooking.dtos;

import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

public record ReservationUpdateRequestDTO(
		LocalDateTime startDate,
		
		@Future(message = "End date must be in the future")
		LocalDateTime endDate
) {
}
