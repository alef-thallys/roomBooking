package com.github.alefthallys.roombooking.api.erros;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record ApiError(
		int status,
		String message,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
		Instant timestamp
) {
	public ApiError(int status, String message) {
		this(status, message, Instant.now());
	}
}
