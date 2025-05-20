package com.github.alefthallys.roombooking.dtos;

public record FieldErrorDTO(
		String field,
		String message,
		Object rejectedValue
) {
}

