package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.exceptions.Auth.ForbiddenException;
import com.github.alefthallys.roombooking.exceptions.Auth.InvalidJwtException;
import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public AuthService(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Transactional(readOnly = true)
	void validateUserOwnership(User userById) {
		UserDetails authentication;
		
		try {
			authentication = jwtTokenProvider.getAuthentication();
		} catch (InvalidJwtException e) {
			throw new ForbiddenException();
		}
		
		if (authentication == null) {
			throw new ForbiddenException();
		}
		
		Optional<? extends GrantedAuthority> authorityOpt = authentication.getAuthorities().stream().findFirst();
		
		if (authorityOpt.isEmpty()) {
			throw new ForbiddenException();
		}
		
		GrantedAuthority authority = authorityOpt.get();
		String username = authentication.getUsername();
		boolean roleAdmin = authority.getAuthority().equals("ROLE_ADMIN");
		
		if (!roleAdmin) {
			if (!userById.getEmail().equals(username)) {
				throw new ForbiddenException();
			}
		}
	}
}
