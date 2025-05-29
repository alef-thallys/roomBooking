package com.github.alefthallys.roombooking.config;

import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public SpringSecurityAuditorAware(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}
		
		return Optional.of(authentication.getName());
	}
}
