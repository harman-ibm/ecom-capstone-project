package com.ecom.ecom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String author;

	@Column(nullable = false)
	private String genre;

	/** Price in cents. */
	@Column(nullable = false)
	private int priceCents;

	private int pages;

	private double rating;

	@Column(length = 2000)
	private String description;

	@Column(length = 2000)
	private String authorBio;

	/** Cover image URL (absolute). */
	@Column(length = 500)
	private String imageUrl;

	/** Author photo URL (absolute). */
	@Column(length = 500)
	private String authorImageUrl;

	public Book(String title, String author, String genre, int priceCents, int pages, double rating,
			String description, String authorBio, String imageUrl, String authorImageUrl) {
		this.title = title;
		this.author = author;
		this.genre = genre;
		this.priceCents = priceCents;
		this.pages = pages;
		this.rating = rating;
		this.description = description;
		this.authorBio = authorBio;
		this.imageUrl = imageUrl;
		this.authorImageUrl = authorImageUrl;
	}
}
