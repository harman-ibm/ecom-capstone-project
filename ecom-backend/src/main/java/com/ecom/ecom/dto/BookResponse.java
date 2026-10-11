package com.ecom.ecom.dto;

/** Prices are in cents; the frontend converts to dollars for display. */
public record BookResponse(
		Long id,
		String title,
		String author,
		String genre,
		int priceCents,
		int pages,
		double rating,
		String description,
		String authorBio,
		String imageUrl,
		String authorImageUrl) {
}
