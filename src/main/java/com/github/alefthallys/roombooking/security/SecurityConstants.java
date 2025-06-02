package com.github.alefthallys.roombooking.security;

import java.util.List;

public class SecurityConstants {
	public static final List<String> PUBLIC_ENDPOINTS = List.of(
			"/api/v1/auth/login",
			"/api/v1/auth/register",
			"/api/v1/auth/refresh-token",
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/actuator/**"
	);
	
	private SecurityConstants() {
	}
}
