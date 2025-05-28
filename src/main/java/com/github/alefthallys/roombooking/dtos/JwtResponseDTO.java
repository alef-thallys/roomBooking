package com.github.alefthallys.roombooking.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JwtResponseDTO(
		String token,
		String refreshToken
) {
}
