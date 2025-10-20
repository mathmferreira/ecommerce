package com.techmath.ecommerce.domain.repositories;

import com.techmath.ecommerce.domain.entities.Order;
import com.techmath.ecommerce.presentation.dto.response.ReportResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o " +
            "INNER JOIN FETCH o.items i " +
            "INNER JOIN FETCH i.product " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(UUID id);

    @Query("SELECT new com.techmath.ecommerce.presentation.dto.response.ReportResponse$TopUserReport(" +
            "u.id, u.name, u.email, COUNT(o), SUM(o.totalAmount)) " +
            "FROM Order o " +
            "INNER JOIN o.user u " +
            "WHERE o.status = 'PAID' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY u.id, u.name, u.email " +
            "ORDER BY COUNT(o) DESC")
    List<ReportResponse.TopUserReport> findTopBuyingUsers(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT new com.techmath.ecommerce.presentation.dto.response.ReportResponse$AverageTicketReport(" +
            "u.id, u.name, u.email, COUNT(o), AVG(o.totalAmount)) " +
            "FROM Order o " +
            "INNER JOIN o.user u " +
            "WHERE o.status = 'PAID' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY u.id, u.name, u.email " +
            "ORDER BY AVG(o.totalAmount) DESC")
    List<ReportResponse.AverageTicketReport> findAverageTicketByUser(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) " +
            "FROM Order o " +
            "WHERE o.status = 'PAID' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal findTotalRevenueByPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
