package com.ecom.ecom.exception;

/** Request is well-formed but breaks a business rule (e.g. expired card). Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {

	public BadRequestException(String message) {
		super(message);
	}
}
