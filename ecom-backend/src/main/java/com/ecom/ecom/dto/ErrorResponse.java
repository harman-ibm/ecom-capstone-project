package com.ecom.ecom.dto;

import java.time.Instant;
import java.util.List;

/** Uniform error body. {@code fieldErrors} is only present for validation failures (HTTP 400). */
public record ErrorResponse(Instant timestamp, int status, String error, String message,
		List<FieldError> fieldErrors) {

	public record FieldError(String field, String message) {
	}
}
