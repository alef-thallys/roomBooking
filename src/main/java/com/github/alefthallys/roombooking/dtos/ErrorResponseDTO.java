package com.github.alefthallys.roombooking.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ErrorResponseDTO(
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
		Instant timestamp,
		int status,
		String error,
		String path,
		Map<String, List<String>> fieldErrors
) {
	public ErrorResponseDTO(int status, String error, String path, Map<String, List<String>> fieldErrors) {
		this(Instant.now(), status, error, path, fieldErrors);
	}
}

