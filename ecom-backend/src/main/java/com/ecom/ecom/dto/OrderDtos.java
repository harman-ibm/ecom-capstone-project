package com.ecom.ecom.dto;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class OrderDtos {

	private OrderDtos() {
	}

	public record ItemRequest(
			@NotNull(message = "Book id is required") Long bookId,
			@Min(value = 1, message = "Quantity must be at least 1") @Max(value = 99, message = "Quantity must be at most 99") int quantity) {
	}

	public record ShippingRequest(
			@NotBlank(message = "Full name is required") @Size(max = 100) String fullName,
			@NotBlank(message = "Street address is required") @Size(max = 200) String address,
			@NotBlank(message = "City is required") @Size(max = 100) String city,
			@NotBlank(message = "State / Province is required") @Size(max = 100) String state,
			@NotBlank(message = "Postal code is required") @Size(max = 20) String postalCode,
			@NotBlank(message = "Country is required") @Size(max = 100) String country,
			@NotBlank(message = "Phone is required") @Pattern(regexp = "^[0-9+()\\-\\s]{5,20}$", message = "Phone number is not valid") String phone,
			@NotBlank(message = "Shipping method is required") @Pattern(regexp = "standard|express", message = "Shipping method must be 'standard' or 'express'") String method) {
	}

	/**
	 * Card details are validated here (format only) and are never stored or logged.
	 * No real payment processing happens: this is a demo payment step.
	 */
	public record PaymentRequest(
			@NotBlank(message = "Name on card is required") @Size(max = 100) String cardName,
			@NotBlank(message = "Card number is required") @Pattern(regexp = "^[0-9 ]{13,23}$", message = "Card number is not valid") String cardNumber,
			@NotBlank(message = "Expiry is required") @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Expiry must be in MM/YY format") String expiry,
			@NotBlank(message = "CVC is required") @Pattern(regexp = "^[0-9]{3,4}$", message = "CVC must be 3 or 4 digits") String cvc) {
	}

	public record CreateOrderRequest(
			@NotEmpty(message = "Order must contain at least one item") @Size(max = 50, message = "Too many items") List<@Valid ItemRequest> items,
			@NotNull(message = "Shipping details are required") @Valid ShippingRequest shipping,
			@NotNull(message = "Payment details are required") @Valid PaymentRequest payment) {
	}

	public record OrderItemResponse(BookResponse book, int quantity, int unitPriceCents, int lineTotalCents) {
	}

	public record ShippingResponse(String fullName, String address, String city, String state, String postalCode,
			String country, String phone, String method) {
	}

	public record OrderResponse(Long id, Instant placedAt, String status, int totalCents,
			ShippingResponse shipping, List<OrderItemResponse> items) {
	}
}
