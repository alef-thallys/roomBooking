package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.exceptions.ForbiddenException;
import com.github.alefthallys.roombooking.exceptions.InvalidJwtException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public AuthService(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Transactional(readOnly = true)
	void validateUserOwnership(User userById) {
		UserDetails authentication = null;
		
		try {
			authentication = jwtTokenProvider.getAuthentication();
		} catch (InvalidJwtException e) {
			throw new ForbiddenException();
		}
		
		if (authentication == null || !authentication.getUsername().equals(userById.getEmail())) {
			throw new ForbiddenException();
		}
	}
}
