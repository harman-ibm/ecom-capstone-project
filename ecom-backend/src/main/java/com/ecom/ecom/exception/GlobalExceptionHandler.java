package com.ecom.ecom.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.ecom.ecom.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<ErrorResponse.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> new ErrorResponse.FieldError(e.getField(), e.getDefaultMessage()))
				.toList();
		// Message = first field error so a client can show a single line.
		String message = fields.isEmpty() ? "Validation failed" : fields.get(0).message();
		return build(HttpStatus.BAD_REQUEST, message, fields);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
		return build(HttpStatus.BAD_REQUEST, "Malformed request", null);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBusinessRule(BadRequestException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
			List<ErrorResponse.FieldError> fields) {
		return ResponseEntity.status(status)
				.body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, fields));
	}
}
