package com.ecom.ecom.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.ecom.dto.BookResponse;
import com.ecom.ecom.exception.BadRequestException;
import com.ecom.ecom.exception.NotFoundException;
import com.ecom.ecom.model.Book;
import com.ecom.ecom.repository.BookRepository;

@Service
@Transactional(readOnly = true)
public class BookService {

	private final BookRepository books;

	public BookService(BookRepository books) {
		this.books = books;
	}

	/** {@code by} is name, author or genre; a blank query returns everything. */
	public List<BookResponse> search(String by, String query) {
		String field = by == null ? "name" : by.toLowerCase(Locale.ROOT);
		if (!List.of("name", "author", "genre").contains(field)) {
			throw new BadRequestException("'by' must be one of: name, author, genre");
		}
		String q = query == null ? "" : query.trim();
		List<Book> result;
		if (q.isEmpty()) {
			result = books.findAll();
		} else {
			result = switch (field) {
				case "author" -> books.findByAuthorContainingIgnoreCase(q);
				case "genre" -> books.findByGenreContainingIgnoreCase(q);
				default -> books.findByTitleContainingIgnoreCase(q);
			};
		}
		return result.stream().sorted(Comparator.comparing(Book::getId)).map(BookMapper::toResponse).toList();
	}

	public BookResponse get(Long id) {
		return BookMapper.toResponse(find(id));
	}

	/** Other books ranked: same genre, then same author, then highest rated. */
	public List<BookResponse> recommendations(Long id, int limit) {
		Book current = find(id);
		int max = Math.min(Math.max(limit, 1), 20);
		return books.findAll().stream()
				.filter(b -> !b.getId().equals(current.getId()))
				.sorted(Comparator
						.comparingInt((Book b) -> score(b, current)).reversed()
						.thenComparing(Comparator.comparingDouble(Book::getRating).reversed())
						.thenComparing(Book::getId))
				.limit(max)
				.map(BookMapper::toResponse)
				.toList();
	}

	private static int score(Book b, Book current) {
		return (b.getGenre().equals(current.getGenre()) ? 2 : 0) + (b.getAuthor().equals(current.getAuthor()) ? 1 : 0);
	}

	private Book find(Long id) {
		return books.findById(id).orElseThrow(() -> new NotFoundException("Book not found"));
	}
}
