package com.github.alefthallys.roombooking.config;


import com.github.alefthallys.roombooking.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingConfig {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	public AuditingConfig(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
	@Bean
	public AuditorAware<String> auditorAware() {
		return new SpringSecurityAuditorAware(jwtTokenProvider);
	}
}
