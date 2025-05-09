package com.github.alefthallys.roombooking.dtos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ErrorResponseDTO(
		LocalDateTime timestamp,
		int status,
		String error,
		String path,
		Map<String, List<String>> fieldErrors
) {
}
