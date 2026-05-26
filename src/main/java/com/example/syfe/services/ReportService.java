package com.example.syfe.services;

import com.example.syfe.dtos.responses.MonthlyReportResponse;
import com.example.syfe.dtos.responses.YearlyReportResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.exceptions.BusinessRuleException;
import com.example.syfe.models.Transaction;
import com.example.syfe.models.User;
import com.example.syfe.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    //Calculates total income by category, total expenses by category, and net savings for a given month.
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        if(month<1 || month>12) 
            throw new BusinessRuleException("Invalid month: "+month+". Month must be between 1 and 12");

        User currentUser = userService.getCurrentUser();
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(currentUser, startDate, endDate);

        return buildMonthlyReport(year, month, transactions);
    }

    //Aggregates category-level income and expenses into a yearly overview.
    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(int year) {
        User currentUser = userService.getCurrentUser();

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(currentUser, startDate, endDate);

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        BigDecimal yearlyIncome = BigDecimal.ZERO;
        BigDecimal yearlyExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getType() == TransactionType.INCOME) {
                yearlyIncome = yearlyIncome.add(t.getAmount());
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                yearlyExpenses = yearlyExpenses.add(t.getAmount());
                expensesByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(yearlyIncome.subtract(yearlyExpenses))
                .build();
    }

    //  Helpers 

    private MonthlyReportResponse buildMonthlyReport(int year, int month, List<Transaction> transactions) {
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());
                expensesByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(totalIncome.subtract(totalExpenses))
                .build();
    }
}
