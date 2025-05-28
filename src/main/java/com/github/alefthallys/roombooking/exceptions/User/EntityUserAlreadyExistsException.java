package com.github.alefthallys.roombooking.exceptions.User;

public class EntityUserAlreadyExistsException extends RuntimeException {
	public EntityUserAlreadyExistsException(String email) {
		super("User already exists with email: " + email);
	}
}
