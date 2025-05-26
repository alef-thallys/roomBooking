package com.github.alefthallys.roombooking.exceptions;

public class RoomNotAvailableException extends RuntimeException {
	public RoomNotAvailableException(Long id) {
		super("Room with ID " + id + " is not available for reservation.");
	}
}
