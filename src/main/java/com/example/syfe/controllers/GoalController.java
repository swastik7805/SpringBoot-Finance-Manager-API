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

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    //POST /api/goals
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //GET /api/goals
    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAllGoals() {
        return ResponseEntity.ok(Map.of("goals", goalService.getAllGoals()));
    }

    //GET /api/goals/{id}
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    //PUT /api/goals/{id}
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, @Valid @RequestBody GoalUpdateRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    //DELETE /api/goals/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted successfully"));
    }
}
