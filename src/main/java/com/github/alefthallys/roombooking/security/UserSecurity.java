package com.github.alefthallys.roombooking.security;

import com.github.alefthallys.roombooking.models.User;
import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public UserSecurity(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	public boolean hasUserId(Long userId) {
		User currentUser = jwtTokenProvider.getCurrentUser();
		
		if (currentUser == null) {
			return false;
		}
		return currentUser.getId().equals(userId);
	}
}