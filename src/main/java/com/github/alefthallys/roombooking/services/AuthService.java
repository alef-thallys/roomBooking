package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.exceptions.ForbiddenException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public AuthService(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	void validateUserOwnership(User userById) {
		UserDetails authentication = jwtTokenProvider.getAuthentication();
		if (!authentication.getUsername().equals(userById.getEmail())) {
			throw new ForbiddenException();
		}
	}
}
