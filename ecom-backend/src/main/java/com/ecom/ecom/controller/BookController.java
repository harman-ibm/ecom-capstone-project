package com.ecom.ecom.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ecom.config.ApiVersion;
import com.ecom.ecom.dto.BookResponse;
import com.ecom.ecom.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiVersion.V1 + "/books")
@Tag(name = "Books", description = "Public catalogue: browse, search and recommendations")
public class BookController {

	private final BookService books;

	public BookController(BookService books) {
		this.books = books;
	}

	@Operation(summary = "List or search books", description = "Search by name, author or genre (case-insensitive, partial match).")
	@ApiResponse(responseCode = "400", description = "Unknown 'by' value")
	@GetMapping
	public List<BookResponse> list(
			@Parameter(description = "Field to search: name | author | genre") @RequestParam(defaultValue = "name") String by,
			@Parameter(description = "Search text; empty returns all books") @RequestParam(name = "q", defaultValue = "") String query) {
		return books.search(by, query);
	}

	@Operation(summary = "Get a book (includes the author bio)")
	@ApiResponse(responseCode = "404", description = "Book not found")
	@GetMapping("/{id}")
	public BookResponse get(@PathVariable Long id) {
		return books.get(id);
	}

	@Operation(summary = "Recommended books", description = "Same genre first, then same author, then highest rated. Excludes the book itself.")
	@ApiResponse(responseCode = "404", description = "Book not found")
	@GetMapping("/{id}/recommendations")
	public List<BookResponse> recommendations(@PathVariable Long id,
			@RequestParam(defaultValue = "5") int limit) {
		return books.recommendations(id, limit);
	}
}
