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

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    //GET /api/reports/monthly/{year}/{month}
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@PathVariable int year,@PathVariable int month) {
        
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    //GET /api/reports/yearly/{year}
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }
}
