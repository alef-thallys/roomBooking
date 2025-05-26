package com.github.alefthallys.roombooking.exceptions;

public class EntityReservationAlreadyExistsException extends RuntimeException {
	public EntityReservationAlreadyExistsException(Long id) {
		super("Reservation with ID " + id + " already exists.");
	}
}
