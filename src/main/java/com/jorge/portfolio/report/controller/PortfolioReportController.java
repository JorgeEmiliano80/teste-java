package com.jorge.portfolio.report.controller;

import com.jorge.portfolio.report.dto.PortfolioSummaryResponse;
import com.jorge.portfolio.report.service.PortfolioReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class PortfolioReportController {

    private final PortfolioReportService portfolioReportService;

    public PortfolioReportController(PortfolioReportService portfolioReportService) {
        this.portfolioReportService = portfolioReportService;
    }

    @GetMapping("/portfolio-summary")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary() {
        return ResponseEntity.ok(portfolioReportService.generatePortfolioSummary());
    }
}
