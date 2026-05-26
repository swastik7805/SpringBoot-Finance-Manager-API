package com.example.syfe.controllers;

import com.example.syfe.dtos.requests.CategoryRequest;
import com.example.syfe.dtos.responses.CategoryResponse;
import com.example.syfe.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing financial categories.
 * Provides endpoints to create, retrieve, and delete custom user categories.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Creates a new custom category for the authenticated user.
     *
     * @param request the category details including name and type (INCOME/EXPENSE)
     * @return the created category response with HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all categories accessible to the authenticated user.
     * This includes default categories and the user's custom categories.
     *
     * @return a map containing a list of accessible categories
     */
    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(Map.of("categories", categories));
    }

    /**
     * Deletes a custom category by its name for the authenticated user.
     * Note: Default categories or categories associated with transactions cannot be deleted.
     *
     * @param name the name of the custom category to delete
     * @return a success message confirming deletion
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable String name) {
        categoryService.deleteCategory(name);
        return ResponseEntity.ok(Map.of("message","Category "+name+" deleted successfully"));
    }
}
