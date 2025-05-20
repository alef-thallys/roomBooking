package com.github.alefthallys.roombooking.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ErrorResponseDTO {
	private int status;
	private String error;
	private String message;
	private String path;
	private String timestamp;
	private List<FieldErrorDTO> fieldErrors;
	
	public ErrorResponseDTO(int status, String error, String message, String path, List<FieldErrorDTO> fieldErrors) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
		this.timestamp = Instant.now().toString();
		this.fieldErrors = fieldErrors;
	}
}
