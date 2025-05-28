package com.github.alefthallys.roombooking.exceptions.Room;

public class EntityRoomNotFoundException extends RuntimeException {
	public EntityRoomNotFoundException(Long id) {
		super("Room not found with id: " + id);
	}
}
