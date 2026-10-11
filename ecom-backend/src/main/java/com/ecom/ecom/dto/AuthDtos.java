package com.ecom.ecom.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record SignupRequest(
			@NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
			@NotBlank(message = "Email is required") @Email(message = "Email must be a valid address") @Size(max = 254) String email,
			@NotBlank(message = "Password is required") @Size(min = 6, max = 100, message = "Password must be 6 to 100 characters") String password,
			@NotBlank(message = "Please confirm your password") String confirmPassword) {

		/** Cross-field rule, enforced here on the server (the UI does no validation of its own). */
		@JsonIgnore
		@Schema(hidden = true)
		@AssertTrue(message = "Passwords do not match")
		public boolean isPasswordsMatch() {
			return password == null || confirmPassword == null || password.equals(confirmPassword);
		}
	}

	public record LoginRequest(
			@NotBlank(message = "Email is required") String email,
			@NotBlank(message = "Password is required") String password) {
	}

	public record UserResponse(Long id, String name, String email) {
	}

	/** Returned by signup and login. {@code token} is a JWT to send as {@code Authorization: Bearer <token>}. */
	public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) {
	}
}
