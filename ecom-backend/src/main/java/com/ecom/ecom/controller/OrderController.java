package com.ecom.ecom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ecom.config.ApiVersion;
import com.ecom.ecom.config.OpenApiConfig;
import com.ecom.ecom.dto.OrderDtos.CreateOrderRequest;
import com.ecom.ecom.dto.OrderDtos.OrderResponse;
import com.ecom.ecom.dto.OrderDtos.ShippingRequest;
import com.ecom.ecom.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(ApiVersion.V1 + "/orders")
@Tag(name = "Orders", description = "Requires authentication. Users only ever see their own orders.")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class OrderController {

	private final OrderService orders;

	public OrderController(OrderService orders) {
		this.orders = orders;
	}

	@Operation(summary = "Place an order",
			description = "Validates shipping and payment, prices the order from the catalogue and stores it. Card data is never stored.")
	@ApiResponse(responseCode = "201", description = "Order placed")
	@ApiResponse(responseCode = "400", description = "Validation failed (see fieldErrors) or business rule broken")
	@ApiResponse(responseCode = "401", description = "Missing or invalid token")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse place(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateOrderRequest request) {
		return orders.place(userId(jwt), request);
	}

	@Operation(summary = "Validate shipping details",
			description = "Runs the server-side shipping validation so the checkout can show errors before the payment step. Nothing is stored.")
	@ApiResponse(responseCode = "204", description = "Shipping details are valid")
	@ApiResponse(responseCode = "400", description = "Validation failed (see fieldErrors)")
	@ApiResponse(responseCode = "401", description = "Missing or invalid token")
	@PostMapping("/validate-shipping")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void validateShipping(@Valid @RequestBody ShippingRequest request) {
		// Validation happens in @Valid; reaching this point means the details are valid.
	}

	@Operation(summary = "My order history", description = "Newest first.")
	@ApiResponse(responseCode = "401", description = "Missing or invalid token")
	@GetMapping
	public List<OrderResponse> list(@AuthenticationPrincipal Jwt jwt) {
		return orders.list(userId(jwt));
	}

	@Operation(summary = "Get one of my orders")
	@ApiResponse(responseCode = "404", description = "Order not found (or belongs to someone else)")
	@GetMapping("/{id}")
	public OrderResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		return orders.get(userId(jwt), id);
	}

	private static Long userId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
