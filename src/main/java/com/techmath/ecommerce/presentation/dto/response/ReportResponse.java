package com.techmath.ecommerce.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

public class ReportResponse {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUserReport {
        private UUID userId;
        private String userName;
        private String userEmail;
        private Long totalOrders;
        private BigDecimal totalSpent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AverageTicketReport {
        private UUID userId;
        private String userName;
        private String userEmail;
        private Long totalOrders;
        private BigDecimal averageTicket;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenueReport {
        private String month;
        private Integer year;
        private BigDecimal totalRevenue;
    }

}
