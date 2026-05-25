package com.example.syfe.services;

import com.example.syfe.dtos.responses.GoalResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.models.Goal;
import com.example.syfe.models.Transaction;
import com.example.syfe.models.User;
import com.example.syfe.repositories.GoalRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private GoalService goalService;

    private User user;
    private Goal goal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        
        goal = Goal.builder()
                .id(1L)
                .goalName("Vacation")
                .targetAmount(new BigDecimal("2000.00"))
                .startDate(LocalDate.now().minusDays(10))
                .targetDate(LocalDate.now().plusDays(30))
                .user(user)
                .build();
    }

    @Test
    void getGoalById_CalculatesProgressCorrectly() {
        // Income since goal start date
        Transaction income = Transaction.builder()
                .amount(new BigDecimal("1500"))
                .type(TransactionType.INCOME)
                .build();

        // Expense since goal start date
        Transaction expense = Transaction.builder()
                .amount(new BigDecimal("500"))
                .type(TransactionType.EXPENSE)
                .build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByUserAndDateBetween(eq(user), eq(goal.getStartDate()), any(LocalDate.class)))
                .thenReturn(List.of(income, expense));

        GoalResponse response = goalService.getGoalById(1L);

        // Progress = Income (1500) - Expense (500) = 1000
        assertEquals(new BigDecimal("1000"), response.getCurrentProgress());
        assertEquals("Vacation", response.getGoalName());
    }
}
