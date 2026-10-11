package com.ecom.ecom.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger UI: /swagger-ui.html. Use "Authorize" and paste the token from /auth/login (without "Bearer ").
 */
@Configuration
public class OpenApiConfig {

	public static final String BEARER = "bearerAuth";

	@Bean
	OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Ecom Bookstore API")
						.version("v1")
						.description("REST API for the bookstore: authentication (JWT), catalogue and orders. "
								+ "All validation is performed on the server. Prices are in cents."))
				.components(new Components().addSecuritySchemes(BEARER,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
	}

	/** One group per API version. Add a "v2" group here when /api/v2 is introduced. */
	@Bean
	GroupedOpenApi v1Api() {
		return GroupedOpenApi.builder().group("v1").pathsToMatch(ApiVersion.V1 + "/**").build();
	}
}
