package com.example.syfe.controllers;

import com.example.syfe.dtos.responses.MonthlyReportResponse;
import com.example.syfe.dtos.responses.YearlyReportResponse;
import com.example.syfe.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for generating financial reports.
 * Provides endpoints for monthly and yearly financial summaries.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generates a monthly financial report.
     * 
     * @param year the year for the report
     * @param month the month for the report (1-12)
     * @return the monthly report response detailing income, expenses, and net savings
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@PathVariable int year,@PathVariable int month) {
        
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    /**
     * Generates a yearly financial report.
     * 
     * @param year the year for the report
     * @return the yearly report response detailing income, expenses, and net savings
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }
}
