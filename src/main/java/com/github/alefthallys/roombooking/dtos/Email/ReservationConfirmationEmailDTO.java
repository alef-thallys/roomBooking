package com.github.alefthallys.roombooking.dtos.Email;

import java.time.LocalDateTime;

public record ReservationConfirmationEmailDTO(
		String recipientEmail,
		String userName,
		String roomName,
		LocalDateTime startDate,
		LocalDateTime endDate,
		Long reservationId
) {
}
