package com.ecom.ecom.exception;

/** A rule violation that isn't a field-format problem (e.g. duplicate email). Maps to HTTP 409. */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
