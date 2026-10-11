package com.ecom.ecom.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bound from {@code app.jwt.*} in application.properties. */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
