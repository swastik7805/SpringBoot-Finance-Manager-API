package com.example.syfe.services;

import com.example.syfe.dtos.responses.MonthlyReportResponse;
import com.example.syfe.dtos.responses.YearlyReportResponse;
import com.example.syfe.enums.TransactionType;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    //Calculates total income by category, total expenses by category, and net savings for a given month.
    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        User currentUser = userService.getCurrentUser();
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(currentUser, startDate, endDate);

        return buildMonthlyReport(year, month, transactions);
    }

    //Aggregates monthly data into a comprehensive yearly overview.
    @Transactional(readOnly = true)
    public YearlyReportResponse getYearlyReport(int year) {
        User currentUser = userService.getCurrentUser();

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository.findByUserAndDateBetween(currentUser, startDate, endDate);

        BigDecimal yearlyIncome = BigDecimal.ZERO;
        BigDecimal yearlyExpenses = BigDecimal.ZERO;
        Map<Integer, YearlyReportResponse.MonthlySummary> monthlyBreakdown = new HashMap<>();

        // Group transactions by month
        Map<Integer, List<Transaction>> transactionsByMonth = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getDate().getMonthValue()));

        for (int month = 1; month <= 12; month++) {
            List<Transaction> monthTransactions = transactionsByMonth.getOrDefault(month, List.of());
            
            BigDecimal monthIncome = sumTransactions(monthTransactions, TransactionType.INCOME);
            BigDecimal monthExpenses = sumTransactions(monthTransactions, TransactionType.EXPENSE);
            
            yearlyIncome = yearlyIncome.add(monthIncome);
            yearlyExpenses = yearlyExpenses.add(monthExpenses);

            monthlyBreakdown.put(month, YearlyReportResponse.MonthlySummary.builder()
                    .totalIncome(monthIncome)
                    .totalExpenses(monthExpenses)
                    .netSavings(monthIncome.subtract(monthExpenses))
                    .build());
        }

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(yearlyIncome)
                .totalExpenses(yearlyExpenses)
                .netSavings(yearlyIncome.subtract(yearlyExpenses))
                .monthlyBreakdown(monthlyBreakdown)
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
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(totalIncome.subtract(totalExpenses))
                .incomeByCategory(incomeByCategory)
                .expensesByCategory(expensesByCategory)
                .build();
    }

    private BigDecimal sumTransactions(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
