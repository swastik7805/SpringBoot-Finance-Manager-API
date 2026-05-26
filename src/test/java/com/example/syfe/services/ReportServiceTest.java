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
import static org.junit.jupiter.api.Assertions.*;
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

        // totalIncome and totalExpenses are now Maps of category -> amount
        assertEquals(new BigDecimal("5000"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("1000"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("4000"), response.getNetSavings());
        assertNotNull(response.getTotalIncome());
        assertNotNull(response.getTotalExpenses());
    }

    @Test
    void getMonthlyReport_EmptyTransactions_ReturnsZeroNetSavings() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        MonthlyReportResponse response = reportService.getMonthlyReport(2023, 10);

        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    void getYearlyReport_AggregatesCorrectly() {
        LocalDate janDate = LocalDate.of(2023, 1, 15);
        Category incomeCat = Category.builder().name("Salary").build();
        Category expenseCat = Category.builder().name("Rent").build();

        Transaction t1 = Transaction.builder()
                .amount(new BigDecimal("5000"))
                .type(TransactionType.INCOME)
                .category(incomeCat)
                .date(janDate)
                .build();

        Transaction t2 = Transaction.builder()
                .amount(new BigDecimal("1000"))
                .type(TransactionType.EXPENSE)
                .category(expenseCat)
                .date(janDate)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(t1, t2));

        YearlyReportResponse response = reportService.getYearlyReport(2023);

        // totalIncome and totalExpenses are now Maps of category -> amount
        assertEquals(new BigDecimal("5000"), response.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("1000"), response.getTotalExpenses().get("Rent"));
        assertEquals(new BigDecimal("4000"), response.getNetSavings());
        assertEquals(2023, response.getYear());
    }
}