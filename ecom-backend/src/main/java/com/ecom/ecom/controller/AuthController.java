package com.ecom.ecom.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ecom.config.ApiVersion;
import com.ecom.ecom.dto.AuthDtos.AuthResponse;
import com.ecom.ecom.dto.AuthDtos.LoginRequest;
import com.ecom.ecom.dto.AuthDtos.SignupRequest;
import com.ecom.ecom.dto.ErrorResponse;
import com.ecom.ecom.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiVersion.V1 + "/auth")
@Tag(name = "Authentication", description = "Create an account and obtain a JWT")
public class AuthController {

	private final AuthService auth;

	public AuthController(AuthService auth) {
		this.auth = auth;
	}

	@Operation(summary = "Create an account", description = "Returns a JWT so the user is signed in immediately.")
	@ApiResponse(responseCode = "201", description = "Account created")
	@ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "409", description = "Email already registered", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
		return auth.signup(request);
	}

	@Operation(summary = "Log in", description = "Returns a JWT. Send it as 'Authorization: Bearer <token>'.")
	@ApiResponse(responseCode = "200", description = "Authenticated")
	@ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return auth.login(request);
	}
}
