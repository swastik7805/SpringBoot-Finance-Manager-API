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
public class MonthlyReportResponse {
    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private Map<String, BigDecimal> incomeByCategory;
    private Map<String, BigDecimal> expensesByCategory;
}
