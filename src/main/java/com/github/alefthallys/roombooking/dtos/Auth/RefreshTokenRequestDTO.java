package com.github.alefthallys.roombooking.dtos.Auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
		@NotBlank(message = "Refresh token is required")
		String refreshToken
) {
}
