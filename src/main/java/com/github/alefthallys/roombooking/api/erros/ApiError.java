package com.github.alefthallys.roombooking.api.erros;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record ApiError(
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
		Instant timestamp,
		int status,
		String message
) {
	public ApiError(int status, String message) {
		this(Instant.now(), status, message);
	}
}
