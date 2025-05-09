package com.github.alefthallys.roombooking.exceptions;

public class EntityRoomAlreadyExistsException extends RuntimeException {
	public EntityRoomAlreadyExistsException(String name) {
		super("Room already exists with name: " + name);
	}
}
