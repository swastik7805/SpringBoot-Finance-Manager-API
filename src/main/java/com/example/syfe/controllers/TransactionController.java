package com.example.syfe.controllers;

import com.example.syfe.dtos.requests.TransactionRequest;
import com.example.syfe.dtos.requests.TransactionUpdateRequest;
import com.example.syfe.dtos.responses.TransactionResponse;
import com.example.syfe.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing financial transactions.
 * Provides endpoints to create, retrieve, filter, update, and delete transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a new financial transaction.
     *
     * @param request the transaction details
     * @return the created transaction response with HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all transactions for the authenticated user, with optional filters.
     *
     * @param startDate the optional start date filter
     * @param endDate the optional end date filter
     * @param categoryId the optional category ID filter
     * @param category the optional category name filter
     * @return a map containing the list of filtered transactions
     */
    @GetMapping
    public ResponseEntity<Map<String, List<TransactionResponse>>> getAllTransactions(
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) String category) {

        List<TransactionResponse> transactions = transactionService.getAllTransactions(startDate, endDate, categoryId, category);
        return ResponseEntity.ok(Map.of("transactions", transactions));
    }

    /**
     * Updates an existing transaction.
     * Note: The transaction date cannot be updated.
     *
     * @param id the ID of the transaction to update
     * @param request the update details
     * @return the updated transaction response
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionUpdateRequest request) {
        TransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param id the ID of the transaction to delete
     * @return a success message confirming deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(Map.of("message", "Transaction deleted successfully"));
    }
}
