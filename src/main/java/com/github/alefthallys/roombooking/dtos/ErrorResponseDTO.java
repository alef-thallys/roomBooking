package com.github.alefthallys.roombooking.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
		int status,
		String error,
		String message,
		String path,
		String timestamp,
		
		@JsonInclude(JsonInclude.Include.NON_NULL)
		List<FieldErrorDTO> fieldErrors
) {
	public ErrorResponseDTO(int status, String error, String message, String path, List<FieldErrorDTO> fieldErrors) {
		this(status, error, message, path, Instant.now().toString(), fieldErrors);
	}
}
