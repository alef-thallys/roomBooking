package com.github.alefthallys.roombooking.exceptions;

import com.github.alefthallys.roombooking.api.erros.ApiError;
import com.github.alefthallys.roombooking.dtos.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(EntityUserNotFoundException.class)
	public ResponseEntity<ApiError> handleUserNotFound(EntityUserNotFoundException ex) {
		return buildApiError(HttpStatus.NOT_FOUND, ex.getMessage());
	}
	
	@ExceptionHandler(EntityUserAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleUserAlreadyExists(EntityUserAlreadyExistsException ex) {
		return buildApiError(HttpStatus.CONFLICT, ex.getMessage());
	}
	
	@ExceptionHandler(EntityRoomNotFoundException.class)
	public ResponseEntity<ApiError> handleRoomNotFound(EntityRoomNotFoundException ex) {
		return buildApiError(HttpStatus.NOT_FOUND, ex.getMessage());
	}
	
	@ExceptionHandler(EntityRoomAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleRoomAlreadyExists(EntityRoomAlreadyExistsException ex) {
		return buildApiError(HttpStatus.CONFLICT, ex.getMessage());
	}
	
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
		return buildApiError(HttpStatus.CONFLICT, ex.getMessage());
	}
	
	
	@ExceptionHandler(InvalidJwtException.class)
	public ResponseEntity<ApiError> handleInvalidJwtException(InvalidJwtException ex) {
		return buildApiError(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		
		Map<String, List<String>> errors = new HashMap<>();
		
		ex.getBindingResult().getFieldErrors().forEach(error -> {
			String field = error.getField();
			String message = error.getDefaultMessage();
			errors.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
		});
		
		ErrorResponseDTO response = new ErrorResponseDTO(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				"Validation Failed",
				request.getRequestURI(),
				errors
		);
		
		log.warn("Validation failed at {}: {}", request.getRequestURI(), errors);
		
		return ResponseEntity.badRequest().body(response);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
			ConstraintViolationException ex,
			HttpServletRequest request) {
		
		Map<String, List<String>> errors = new HashMap<>();
		
		ex.getConstraintViolations().forEach(violation -> {
			String field = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			errors.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
		});
		
		ErrorResponseDTO response = new ErrorResponseDTO(
				HttpStatus.BAD_REQUEST.value(),
				"Constraint Violation",
				request.getRequestURI(),
				errors
		);
		
		log.warn("Constraint violation at {}: {}", request.getRequestURI(), errors);
		
		return ResponseEntity.badRequest().body(response);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGenericException(Exception ex) {
		log.error("Unexpected error occurred: ", ex);
		return buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred. Please try again later.");
	}
	
	private ResponseEntity<ApiError> buildApiError(HttpStatus status, String message) {
		ApiError apiError = new ApiError(status.value(), message);
		return new ResponseEntity<>(apiError, status);
	}
}
