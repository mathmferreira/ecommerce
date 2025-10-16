package com.techmath.ecommerce.domain.repositories;

import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserId(UUID userId);

    List<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

    @Query("SELECT o FROM Order o " +
            "INNER JOIN FETCH o.items i " +
            "INNER JOIN FETCH i.product " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(UUID id);

}
