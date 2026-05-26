package com.example.syfe.controllers;

import com.example.syfe.dtos.requests.GoalRequest;
import com.example.syfe.dtos.requests.GoalUpdateRequest;
import com.example.syfe.dtos.responses.GoalResponse;
import com.example.syfe.services.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing savings goals.
 * Provides endpoints for creating, retrieving, updating, and deleting financial goals.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * Creates a new savings goal for the authenticated user.
     *
     * @param request the goal details including name, target amount, and dates
     * @return the created goal response with HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all savings goals for the authenticated user.
     *
     * @return a map containing a list of the user's goals and their progress
     */
    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAllGoals() {
        return ResponseEntity.ok(Map.of("goals", goalService.getAllGoals()));
    }

    /**
     * Retrieves a specific savings goal by its ID.
     *
     * @param id the ID of the goal to retrieve
     * @return the goal response including progress calculations
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    /**
     * Updates an existing savings goal (e.g., modifying target amount or date).
     *
     * @param id the ID of the goal to update
     * @param request the update details
     * @return the updated goal response
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, @Valid @RequestBody GoalUpdateRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    /**
     * Deletes a savings goal by its ID.
     *
     * @param id the ID of the goal to delete
     * @return a success message confirming deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
