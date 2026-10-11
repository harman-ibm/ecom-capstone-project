package com.ecom.ecom.service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.ecom.dto.OrderDtos.CreateOrderRequest;
import com.ecom.ecom.dto.OrderDtos.ItemRequest;
import com.ecom.ecom.dto.OrderDtos.OrderResponse;
import com.ecom.ecom.dto.OrderDtos.PaymentRequest;
import com.ecom.ecom.dto.OrderDtos.ShippingRequest;
import com.ecom.ecom.exception.BadRequestException;
import com.ecom.ecom.exception.NotFoundException;
import com.ecom.ecom.model.Book;
import com.ecom.ecom.model.Order;
import com.ecom.ecom.model.OrderItem;
import com.ecom.ecom.model.ShippingAddress;
import com.ecom.ecom.model.User;
import com.ecom.ecom.repository.BookRepository;
import com.ecom.ecom.repository.OrderRepository;
import com.ecom.ecom.repository.UserRepository;

@Service
public class OrderService {

	private static final DateTimeFormatter EXPIRY = DateTimeFormatter.ofPattern("MM/yy");

	private final OrderRepository orders;
	private final BookRepository books;
	private final UserRepository users;

	public OrderService(OrderRepository orders, BookRepository books, UserRepository users) {
		this.orders = orders;
		this.books = books;
		this.users = users;
	}

	/**
	 * Prices and totals come from the database, never from the client.
	 * Card data is only checked for plausibility and is discarded; it is never persisted or logged.
	 */
	@Transactional
	public OrderResponse place(Long userId, CreateOrderRequest request) {
		validatePayment(request.payment());
		User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

		// Merge duplicate lines for the same book.
		Map<Long, Integer> quantities = new LinkedHashMap<>();
		for (ItemRequest item : request.items()) {
			quantities.merge(item.bookId(), item.quantity(), Integer::sum);
		}

		Order order = new Order();
		order.setUser(user);
		order.setPlacedAt(Instant.now());
		order.setStatus("Processing");
		order.setShipping(toAddress(request.shipping()));

		int total = 0;
		for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
			if (entry.getValue() > 99) {
				throw new BadRequestException("Quantity must be at most 99 per book");
			}
			Book book = books.findById(entry.getKey())
					.orElseThrow(() -> new BadRequestException("Book " + entry.getKey() + " does not exist"));
			OrderItem item = new OrderItem();
			item.setBook(book);
			item.setQuantity(entry.getValue());
			item.setUnitPriceCents(book.getPriceCents());
			order.addItem(item);
			total += book.getPriceCents() * entry.getValue();
		}
		order.setTotalCents(total);
		return BookMapper.toResponse(orders.save(order));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> list(Long userId) {
		return orders.findByUserIdOrderByPlacedAtDescIdDesc(userId).stream().map(BookMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse get(Long userId, Long orderId) {
		// Scoped by user id: another user's order looks exactly like a missing one.
		return orders.findByIdAndUserId(orderId, userId).map(BookMapper::toResponse)
				.orElseThrow(() -> new NotFoundException("Order not found"));
	}

	private static void validatePayment(PaymentRequest payment) {
		String digits = payment.cardNumber().replace(" ", "");
		if (!digits.matches("\\d{16}")) {
			throw new BadRequestException("Card number must be 16 digits");
		}
		YearMonth expiry = YearMonth.parse(payment.expiry(), EXPIRY);
		if (expiry.isBefore(YearMonth.now())) {
			throw new BadRequestException("Card has expired");
		}
	}

	private static ShippingAddress toAddress(ShippingRequest s) {
		ShippingAddress a = new ShippingAddress();
		a.setFullName(s.fullName().trim());
		a.setAddress(s.address().trim());
		a.setCity(s.city().trim());
		a.setState(s.state().trim());
		a.setPostalCode(s.postalCode().trim());
		a.setCountry(s.country().trim());
		a.setPhone(s.phone().trim());
		a.setMethod(s.method());
		return a;
	}
}
