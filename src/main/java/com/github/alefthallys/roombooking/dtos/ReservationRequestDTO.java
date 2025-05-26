package com.github.alefthallys.roombooking.dtos;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequestDTO(
		@NotNull(message = "Room ID cannot be null")
		Long roomId,
		
		@NotNull(message = "Start date cannot be null")
		LocalDateTime startDate,
		
		@Future(message = "End date must be in the future")
		@NotNull(message = "End date cannot be null")
		LocalDateTime endDate
) {
}
