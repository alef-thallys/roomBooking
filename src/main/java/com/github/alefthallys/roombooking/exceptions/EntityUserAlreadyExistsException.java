package com.github.alefthallys.roombooking.exceptions;

public class EntityUserAlreadyExistsException extends RuntimeException {
	public EntityUserAlreadyExistsException(String email) {
		super("User already exists with email: " + email);
	}
}
