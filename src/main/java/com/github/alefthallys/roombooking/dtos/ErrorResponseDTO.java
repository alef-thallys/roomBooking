package com.github.alefthallys.roombooking.dtos;

import java.time.Instant;
import java.util.List;

public record ErrorResponseDTO(
		int status,
		String error,
		String message,
		String path,
		String timestamp,
		List<FieldErrorDTO> fieldErrors
) {
	public ErrorResponseDTO(int status, String error, String message, String path, List<FieldErrorDTO> fieldErrors) {
		this(status, error, message, path, Instant.now().toString(), fieldErrors);
	}
}
