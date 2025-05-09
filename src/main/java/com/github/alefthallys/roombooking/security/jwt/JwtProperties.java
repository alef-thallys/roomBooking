package com.github.alefthallys.roombooking.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	
	private String secret;
	private long expiration;
	private String issuer;
	private String audience;
	private String header;
	private String prefix;
	private Token token = new Token();
	
	@Setter
	public static class Token {
		private long refreshExpiration;
		private String header;
		private String prefix;
	}
}
