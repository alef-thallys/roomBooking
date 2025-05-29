package com.github.alefthallys.roombooking.exceptions;

import java.time.LocalDateTime;

public class EntityReservationConflictException extends RuntimeException {
	public EntityReservationConflictException(LocalDateTime existingStartDate, LocalDateTime existingEndDate, String roomName) {
		super(String.format("The room '%s' is already reserved from %s to %s.", roomName, existingStartDate, existingEndDate));
	}
}
