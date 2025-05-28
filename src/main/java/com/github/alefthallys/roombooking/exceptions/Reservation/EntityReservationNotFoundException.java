package com.github.alefthallys.roombooking.exceptions.Reservation;

public class EntityReservationNotFoundException extends RuntimeException {
	public EntityReservationNotFoundException(Long id) {
		super("Reservation not found with id: " + id);
	}
}
