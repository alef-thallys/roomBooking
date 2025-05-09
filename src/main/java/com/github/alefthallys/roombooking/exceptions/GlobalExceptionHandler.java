package com.github.alefthallys.roombooking.exceptions;

import com.github.alefthallys.roombooking.api.erros.ApiError;
import com.github.alefthallys.roombooking.dtos.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				LocalDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				"Validation Failed",
				request.getRequestURI(),
				errors
		);
		
		return ResponseEntity.badRequest().body(response);
	}
}
