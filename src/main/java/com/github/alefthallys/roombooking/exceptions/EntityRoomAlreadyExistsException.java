package com.github.alefthallys.roombooking.exceptions;

public class EntityRoomAlreadyExistsException extends RuntimeException {
	public EntityRoomAlreadyExistsException(String description) {
		super("Room already exists with description: " + description);
	}
}
