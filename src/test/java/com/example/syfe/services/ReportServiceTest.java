package com.example.syfe.services;

import com.example.syfe.dtos.responses.MonthlyReportResponse;
import com.example.syfe.dtos.responses.YearlyReportResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.models.Category;
import com.example.syfe.models.Transaction;
import com.example.syfe.models.User;
import com.example.syfe.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportService reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
    }

    @Test
    void getMonthlyReport_AggregatesCorrectly() {
        Category incomeCat = Category.builder().name("Salary").build();
        Category expenseCat = Category.builder().name("Food").build();

        Transaction t1 = Transaction.builder()
                .amount(new BigDecimal("5000"))
                .type(TransactionType.INCOME)
                .category(incomeCat)
                .build();

        Transaction t2 = Transaction.builder()
                .amount(new BigDecimal("1000"))
                .type(TransactionType.EXPENSE)
                .category(expenseCat)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        MonthlyReportResponse response = reportService.getMonthlyReport(2023, 10);

        assertEquals(new BigDecimal("5000"), response.getTotalIncome());
        assertEquals(new BigDecimal("1000"), response.getTotalExpenses());
        assertEquals(new BigDecimal("4000"), response.getNetSavings());
        assertEquals(new BigDecimal("5000"), response.getIncomeByCategory().get("Salary"));
        assertEquals(new BigDecimal("1000"), response.getExpensesByCategory().get("Food"));
    }

    @Test
    void getYearlyReport_AggregatesCorrectly() {
        LocalDate janDate = LocalDate.of(2023, 1, 15);
        Category incomeCat = Category.builder().name("Salary").build();
        
        Transaction t1 = Transaction.builder()
                .amount(new BigDecimal("5000"))
                .type(TransactionType.INCOME)
                .category(incomeCat)
                .date(janDate)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1));

        YearlyReportResponse response = reportService.getYearlyReport(2023);

        assertEquals(new BigDecimal("5000"), response.getTotalIncome());
        assertEquals(new BigDecimal("0"), response.getTotalExpenses());
        assertEquals(new BigDecimal("5000"), response.getNetSavings());
        
        // Month 1 (January) should have the 5000 income
        assertEquals(new BigDecimal("5000"), response.getMonthlyBreakdown().get(1).getTotalIncome());
        // Month 2 (February) should have 0 income
        assertEquals(new BigDecimal("0"), response.getMonthlyBreakdown().get(2).getTotalIncome());
    }
}
