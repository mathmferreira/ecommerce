package com.techmath.ecommerce.presentation.controllers;

import com.techmath.ecommerce.application.services.ReportService;
import com.techmath.ecommerce.presentation.dto.response.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/top-buyers")
    public List<ReportResponse.TopUserReport> getTopBuyingUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<ReportResponse.TopUserReport> report = reportService.getTopBuyingUsers(startDate, endDate);
        return report.stream().limit(5).toList();
    }

    @GetMapping("/average-ticket")
    public List<ReportResponse.AverageTicketReport> getAverageTicketByUser(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reportService.getAverageTicketByUser(startDate, endDate);
    }

    @GetMapping("/current-month-revenue")
    public ReportResponse.MonthlyRevenueReport getCurrentMonthRevenue() {
        return reportService.getCurrentMonthRevenue();
    }

    @GetMapping("/revenue")
    public ReportResponse.MonthlyRevenueReport getRevenueByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reportService.getRevenueByPeriod(startDate, endDate);
    }

}
