package com.example.syfe.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearlyReportResponse {
    private int year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private Map<Integer, MonthlySummary> monthlyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netSavings;
    }
}
