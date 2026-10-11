package com.ecom.ecom.service;

import com.ecom.ecom.dto.BookResponse;
import com.ecom.ecom.dto.OrderDtos.OrderItemResponse;
import com.ecom.ecom.dto.OrderDtos.OrderResponse;
import com.ecom.ecom.dto.OrderDtos.ShippingResponse;
import com.ecom.ecom.model.Book;
import com.ecom.ecom.model.Order;
import com.ecom.ecom.model.OrderItem;
import com.ecom.ecom.model.ShippingAddress;

/** Entity -> DTO mapping. Entities are never serialized directly. */
final class BookMapper {

	private BookMapper() {
	}

	static BookResponse toResponse(Book b) {
		return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getGenre(), b.getPriceCents(),
				b.getPages(), b.getRating(), b.getDescription(), b.getAuthorBio(), b.getImageUrl(),
				b.getAuthorImageUrl());
	}

	static OrderResponse toResponse(Order o) {
		return new OrderResponse(o.getId(), o.getPlacedAt(), o.getStatus(), o.getTotalCents(),
				toResponse(o.getShipping()), o.getItems().stream().map(BookMapper::toResponse).toList());
	}

	private static OrderItemResponse toResponse(OrderItem i) {
		return new OrderItemResponse(toResponse(i.getBook()), i.getQuantity(), i.getUnitPriceCents(),
				i.getUnitPriceCents() * i.getQuantity());
	}

	private static ShippingResponse toResponse(ShippingAddress s) {
		return new ShippingResponse(s.getFullName(), s.getAddress(), s.getCity(), s.getState(), s.getPostalCode(),
				s.getCountry(), s.getPhone(), s.getMethod());
	}
}
