package com.techmath.ecommerce.application.services;

import com.techmath.ecommerce.domain.repositories.OrderRepository;
import com.techmath.ecommerce.presentation.dto.response.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;

    public List<ReportResponse.TopUserReport> getTopBuyingUsers(LocalDate startDate, LocalDate endDate) {
        var start = Objects.nonNull(startDate) ? startDate.atStartOfDay() : LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = Objects.nonNull(endDate) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        return orderRepository.findTopBuyingUsers(start, end);
    }

    public List<ReportResponse.AverageTicketReport> getAverageTicketByUser(LocalDate startDate, LocalDate endDate) {
        var start = Objects.nonNull(startDate) ? startDate.atStartOfDay() : LocalDateTime.of(2025, 1, 1, 0, 0);
        var end = Objects.nonNull(endDate) ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        return orderRepository.findAverageTicketByUser(start, end);
    }

    public ReportResponse.MonthlyRevenueReport getCurrentMonthRevenue() {
        var currentMonth = YearMonth.now();
        var startOfMonth = currentMonth.atDay(1).atStartOfDay();
        var endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        var totalRevenue = orderRepository.findTotalRevenueByPeriod(startOfMonth, endOfMonth);

        if (Objects.isNull(totalRevenue)) {
            totalRevenue = BigDecimal.ZERO;
        }

        return new ReportResponse.MonthlyRevenueReport(currentMonth.getMonth().toString(), currentMonth.getYear(), totalRevenue);
    }

    public ReportResponse.MonthlyRevenueReport getRevenueByPeriod(LocalDate startDate, LocalDate endDate) {
        var start = startDate.atStartOfDay();
        var end = endDate.atTime(23, 59, 59);

        var totalRevenue = orderRepository.findTotalRevenueByPeriod(start, end);

        if (Objects.isNull(totalRevenue)) {
            totalRevenue = BigDecimal.ZERO;
        }

        return new ReportResponse.MonthlyRevenueReport("CUSTOM_PERIOD", 0, totalRevenue);
    }

}
