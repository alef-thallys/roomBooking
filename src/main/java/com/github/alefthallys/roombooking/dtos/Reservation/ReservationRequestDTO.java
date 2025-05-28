package com.github.alefthallys.roombooking.dtos.Reservation;


import com.github.alefthallys.roombooking.annotations.ValidReservationDates;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@ValidReservationDates
public record ReservationRequestDTO(
		@NotNull(message = "Room ID is required")
		Long roomId,
		
		@NotNull(message = "Start date is required")
		@FutureOrPresent(message = "Start date must be today or in the future")
		LocalDateTime startDate,
		
		@NotNull(message = "End date is required")
		@Future(message = "End date must be in the future")
		LocalDateTime endDate
) {
}
