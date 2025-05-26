package com.github.alefthallys.roombooking.exceptions;

public class EntityUserNotFoundException extends RuntimeException {
	
	public EntityUserNotFoundException(Long id) {
		super("User not found with id: " + id);
	}
}
