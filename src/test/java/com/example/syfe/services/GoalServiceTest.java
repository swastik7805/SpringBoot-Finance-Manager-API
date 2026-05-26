package com.example.syfe.services;

import com.example.syfe.dtos.responses.GoalResponse;
import com.example.syfe.models.Goal;
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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
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
        // Progress is now calculated via a single DB-side query (calculateNetSavingsSinceStartDate)
        BigDecimal netSavings = new BigDecimal("1000");

        when(userService.getCurrentUser()).thenReturn(user);
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.calculateNetSavingsSinceStartDate(eq(user), eq(goal.getStartDate())))
                .thenReturn(netSavings);

        GoalResponse response = goalService.getGoalById(1L);

        // Progress = net savings returned by DB query = 1000
        assertEquals(new BigDecimal("1000"), response.getCurrentProgress());
        assertEquals("Vacation", response.getGoalName());
        // remainingAmount = targetAmount (2000) - currentProgress (1000) = 1000
        assertEquals(new BigDecimal("1000.00"), response.getRemainingAmount());
        // progressPercentage = 1000 / 2000 * 100 = 50.0
        assertEquals(50.0, response.getProgressPercentage());
    }

    @Test
    void getGoalById_NoTransactions_ReturnsZeroProgress() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(goalRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(goal));
        when(transactionRepository.calculateNetSavingsSinceStartDate(eq(user), eq(goal.getStartDate())))
                .thenReturn(BigDecimal.ZERO);

        GoalResponse response = goalService.getGoalById(1L);

        assertEquals(BigDecimal.ZERO, response.getCurrentProgress());
        assertEquals(0.0, response.getProgressPercentage());
        assertEquals(new BigDecimal("2000.00"), response.getRemainingAmount());
    }
}
