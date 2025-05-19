package com.github.alefthallys.roombooking.exceptions;

public class InvalidJwtException extends RuntimeException {
	public InvalidJwtException(String message) {
		super(message);
	}
}
