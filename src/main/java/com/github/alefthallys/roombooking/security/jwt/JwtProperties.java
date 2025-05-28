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
	private String refreshSecret;
	private long refreshExpiration;
}
