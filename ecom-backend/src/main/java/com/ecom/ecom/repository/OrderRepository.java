package com.ecom.ecom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.ecom.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	/** Items and books are fetched eagerly so the mapper can run outside a transaction (open-in-view is off). */
	@EntityGraph(attributePaths = { "items", "items.book" })
	List<Order> findByUserIdOrderByPlacedAtDescIdDesc(Long userId);

	@EntityGraph(attributePaths = { "items", "items.book" })
	Optional<Order> findByIdAndUserId(Long id, Long userId);
}
