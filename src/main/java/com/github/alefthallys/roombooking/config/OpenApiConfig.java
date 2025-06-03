package com.github.alefthallys.roombooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	
	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "bearerAuth";
		
		return new OpenAPI()
				.info(new Info()
						.title("Room Booking API")
						.version("1.0")
						.description("API for managing room reservations"))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName,
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")));
	}
}
