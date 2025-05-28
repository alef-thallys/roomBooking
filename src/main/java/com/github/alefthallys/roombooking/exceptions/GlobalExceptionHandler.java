package com.github.alefthallys.roombooking.exceptions;

import com.github.alefthallys.roombooking.dtos.ErrorResponseDTO;
import com.github.alefthallys.roombooking.dtos.FieldErrorDTO;
import com.github.alefthallys.roombooking.exceptions.Reservation.EntityReservationAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.Reservation.EntityReservationNotFoundException;
import com.github.alefthallys.roombooking.exceptions.Room.EntityRoomAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.Room.EntityRoomNotFoundException;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserAlreadyExistsException;
import com.github.alefthallys.roombooking.exceptions.User.EntityUserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private ResponseEntity<ErrorResponseDTO> buildErrorResponse(HttpStatus status, String message, String path) {
		ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
				status.value(),
				status.getReasonPhrase(),
				message,
				path,
				null
		);
		return new ResponseEntity<>(errorResponseDTO, status);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		
		log.warn("Validation failed at {}: {}", request.getRequestURI(), ex.getMessage());
		
		List<FieldErrorDTO> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new FieldErrorDTO(
						error.getField(),
						error.getDefaultMessage(),
						error.getRejectedValue()))
				.toList();
		
		ErrorResponseDTO errorResponse = new ErrorResponseDTO(
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Validation failed",
				request.getRequestURI(),
				fieldErrors
		);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(EntityUserNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleUserNotFound(EntityUserNotFoundException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(EntityUserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExists(EntityUserAlreadyExistsException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI());
	}
	
	@ExceptionHandler(EntityRoomNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleRoomNotFound(EntityRoomNotFoundException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(EntityRoomAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDTO> handleRoomAlreadyExists(EntityRoomAlreadyExistsException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(EntityReservationNotFoundException.class)
	public ResponseEntity<ErrorResponseDTO> handleReservationNotFound(EntityReservationNotFoundException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(EntityReservationAlreadyExistsException.class)
	public ResponseEntity<ErrorResponseDTO> handleReservationAlreadyExists(EntityReservationAlreadyExistsException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorResponseDTO> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
	}
	
	@ExceptionHandler({Exception.class, RuntimeException.class})
	public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred. Please try again later.", request.getRequestURI());
	}
}
