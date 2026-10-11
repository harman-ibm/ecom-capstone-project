package com.ecom.ecom.service;

import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecom.ecom.dto.AuthDtos.AuthResponse;
import com.ecom.ecom.dto.AuthDtos.LoginRequest;
import com.ecom.ecom.dto.AuthDtos.SignupRequest;
import com.ecom.ecom.dto.AuthDtos.UserResponse;
import com.ecom.ecom.exception.ConflictException;
import com.ecom.ecom.model.User;
import com.ecom.ecom.repository.UserRepository;
import com.ecom.ecom.security.JwtService;

@Service
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final JwtService jwt;

	public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
		this.users = users;
		this.encoder = encoder;
		this.jwt = jwt;
	}

	public AuthResponse signup(SignupRequest request) {
		String email = normalize(request.email());
		if (users.existsByEmail(email)) {
			throw new ConflictException("An account with this email already exists");
		}
		User user = users.save(new User(request.name().trim(), email, encoder.encode(request.password())));
		return respond(user);
	}

	public AuthResponse login(LoginRequest request) {
		// Same message for unknown email and wrong password so accounts can't be enumerated.
		User user = users.findByEmail(normalize(request.email()))
				.filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		return respond(user);
	}

	private AuthResponse respond(User user) {
		return new AuthResponse(jwt.createToken(user), "Bearer", jwt.expiresInSeconds(),
				new UserResponse(user.getId(), user.getName(), user.getEmail()));
	}

	private static String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
