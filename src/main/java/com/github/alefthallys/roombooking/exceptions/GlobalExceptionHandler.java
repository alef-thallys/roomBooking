package com.github.alefthallys.roombooking.exceptions;

import com.github.alefthallys.roombooking.api.erros.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(EntityUserNotFoundException.class)
	public ResponseEntity<ApiError> handleEntityNotFound(EntityUserNotFoundException ex) {
		ApiError apiError = new ApiError(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage()
		);
		return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(EntityUserAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleEntityAlreadyExists(EntityUserAlreadyExistsException ex) {
		ApiError apiError = new ApiError(
				HttpStatus.CONFLICT.value(),
				ex.getMessage()
		);
		return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(EntityRoomNotFoundException.class)
	public ResponseEntity<ApiError> handleEntityNotFound(EntityRoomNotFoundException ex) {
		ApiError apiError = new ApiError(
				HttpStatus.NOT_FOUND.value(),
				ex.getMessage()
		);
		return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(EntityRoomAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleEntityAlreadyExists(EntityRoomAlreadyExistsException ex) {
		ApiError apiError = new ApiError(
				HttpStatus.CONFLICT.value(),
				ex.getMessage()
		);
		return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
		ApiError apiError = new ApiError(
				HttpStatus.BAD_REQUEST.value(),
				ex.getMessage()
		);
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}
}
