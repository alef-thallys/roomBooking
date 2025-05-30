package com.github.alefthallys.roombooking.exceptions.Auth;

public class InvalidJwtException extends RuntimeException {
	
	public InvalidJwtException(String message) {
		super(message);
	}
	
	public InvalidJwtException(String message, Throwable cause) {
		super(message, cause);
	}
}
