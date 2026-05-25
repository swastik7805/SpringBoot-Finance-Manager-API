package com.example.syfe.services;

import com.example.syfe.dtos.requests.GoalRequest;
import com.example.syfe.dtos.requests.GoalUpdateRequest;
import com.example.syfe.dtos.responses.GoalResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.exceptions.BusinessRuleException;
import com.example.syfe.exceptions.DuplicateResourceException;
import com.example.syfe.exceptions.ResourceNotFoundException;
import com.example.syfe.models.Goal;
import com.example.syfe.models.Transaction;
import com.example.syfe.models.User;
import com.example.syfe.repositories.GoalRepository;
import com.example.syfe.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    //Creates a new savings goal.
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        User currentUser = userService.getCurrentUser();

        // Check for duplicates
        if (goalRepository.existsByGoalNameAndUser(request.getGoalName().trim(), currentUser)) {
            throw new DuplicateResourceException("Goal", "goalName", request.getGoalName());
        }

        // Validate dates
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Target date must be in the future");
        }
        if (startDate.isAfter(request.getTargetDate())) {
            throw new BusinessRuleException("Start date cannot be after the target date");
        }

        Goal goal = Goal.builder()
                .goalName(request.getGoalName().trim())
                .targetAmount(request.getTargetAmount())
                .startDate(startDate)
                .targetDate(request.getTargetDate())
                .user(currentUser)
                .build();

        Goal saved = goalRepository.save(goal);
        return mapToResponse(saved, calculateProgress(saved, currentUser));
    }

    //Retrieves all goals for the logged-in user.
    @Transactional(readOnly = true)
    public List<GoalResponse> getAllGoals() {
        User currentUser = userService.getCurrentUser();
        
        return goalRepository.findAllByUserOrderByTargetDateAsc(currentUser)
                .stream()
                .map(goal->mapToResponse(goal, calculateProgress(goal, currentUser)))
                .toList();
    }

    //Retrieves a single goal by its ID.
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(Long id) {
        User currentUser = userService.getCurrentUser();
        Goal goal = goalRepository.findByIdAndUser(id, currentUser).orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        return mapToResponse(goal, calculateProgress(goal, currentUser));
    }

    //Updates an existing goal's target amount and target date.
    @Transactional
    public GoalResponse updateGoal(Long id, GoalUpdateRequest request) {
        User currentUser = userService.getCurrentUser();

        Goal goal=goalRepository.findByIdAndUser(id,currentUser).orElseThrow(()->new ResourceNotFoundException("Goal", "id", id));

        // Validate new target date
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Target date must be in the future");
        }
        if (goal.getStartDate().isAfter(request.getTargetDate())) {
            throw new BusinessRuleException("Target date cannot be before the start date");
        }

        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());

        Goal updated = goalRepository.save(goal);
        return mapToResponse(updated, calculateProgress(updated, currentUser));
    }

    //Deletes a goal.
    @Transactional
    public void deleteGoal(Long id) {
        User currentUser = userService.getCurrentUser();

        Goal goal = goalRepository.findByIdAndUser(id,currentUser).orElseThrow(()->new ResourceNotFoundException("Goal", "id", id));

        goalRepository.delete(goal);
    }

    //Helpers 
    private BigDecimal calculateProgress(Goal goal, User user) {
        List<Transaction> transactionsSinceStart = transactionRepository.findByUserAndDateBetween(user, goal.getStartDate(), LocalDate.now());

        BigDecimal totalIncome = transactionsSinceStart.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactionsSinceStart.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.subtract(totalExpenses);
    }

    private GoalResponse mapToResponse(Goal goal, BigDecimal currentProgress) {
        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .currentProgress(currentProgress)
                .startDate(goal.getStartDate())
                .targetDate(goal.getTargetDate())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
