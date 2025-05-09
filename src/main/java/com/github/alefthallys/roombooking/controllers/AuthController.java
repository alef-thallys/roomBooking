package com.github.alefthallys.roombooking.controllers;

import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	
	public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}
}
