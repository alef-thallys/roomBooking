package com.github.alefthallys.roombooking.exceptions.User;

public class EntityUserNotFoundException extends RuntimeException {
	
	public EntityUserNotFoundException(Long id) {
		super("User not found with id: " + id);
	}
}
