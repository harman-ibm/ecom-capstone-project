package com.ecom.ecom.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.ecom.ecom.model.User;

@Service
public class JwtService {

	private final JwtEncoder encoder;
	private final JwtProperties properties;

	public JwtService(JwtEncoder encoder, JwtProperties properties) {
		this.encoder = encoder;
		this.properties = properties;
	}

	public long expiresInSeconds() {
		return properties.expirationMinutes() * 60;
	}

	/** Subject is the user id; the email is included as a convenience claim. */
	public String createToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("ecom")
				.issuedAt(now)
				.expiresAt(now.plus(properties.expirationMinutes(), ChronoUnit.MINUTES))
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
