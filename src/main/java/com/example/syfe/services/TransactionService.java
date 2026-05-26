package com.example.syfe.services;

import com.example.syfe.dtos.requests.TransactionRequest;
import com.example.syfe.dtos.requests.TransactionUpdateRequest;
import com.example.syfe.dtos.responses.TransactionResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.exceptions.BusinessRuleException;
import com.example.syfe.exceptions.ResourceNotFoundException;
import com.example.syfe.models.Category;
import com.example.syfe.models.Transaction;
import com.example.syfe.models.User;
import com.example.syfe.repositories.CategoryRepository;
import com.example.syfe.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing financial transactions.
 * Handles business logic for creating, retrieving, updating, and deleting user transactions.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    /**
     * Creates a new transaction for the current user.
     * Validates that the transaction date is not in the future and the category is accessible.
     *
     * @param request the transaction details
     * @return the created transaction response
     * @throws BusinessRuleException if the transaction date is in the future
     * @throws ResourceNotFoundException if the specified category is not found
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = userService.getCurrentUser();

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Transaction date cannot be in the future");
        }

        List<Category> categories = categoryRepository.findAccessibleByName(request.getCategory().trim(), currentUser);
        if (categories.isEmpty()) {
            throw new ResourceNotFoundException("Category", "name", request.getCategory());
        }
        Category category = categories.get(0);
        TransactionType type = category.getType();

        Transaction transaction=Transaction.builder()
                                .amount(request.getAmount())
                                .description(request.getDescription())
                                .date(request.getDate())
                                .type(type)
                                .category(category)
                                .user(currentUser)
                                .build();

        Transaction saved=transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Fetches all transactions for the logged-in user with optional filters.
     *
     * @param startDate the optional start date filter
     * @param endDate the optional end date filter
     * @param categoryId the optional category ID filter
     * @param category the optional category name filter
     * @return a list of filtered transaction responses
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(LocalDate startDate, LocalDate endDate, Long categoryId, String category) {
        User currentUser = userService.getCurrentUser();

        // If category name is present and categoryId is not
        Long resolvedCategoryId = categoryId;
        if (resolvedCategoryId==null && category!=null && !category.trim().isEmpty()) {
            List<Category> categories=categoryRepository.findAccessibleByName(category.trim(), currentUser);
            if(!categories.isEmpty()) resolvedCategoryId=categories.get(0).getId();
            else return List.of();
        }

        return transactionRepository.findAllByUserWithFilters(currentUser,startDate,endDate,resolvedCategoryId)
                                    .stream()
                                    .map(this::mapToResponse)
                                    .toList();
    }

    /**
     * Updates an existing transaction. Only provided fields are updated.
     *
     * @param id the ID of the transaction to update
     * @param request the update details
     * @return the updated transaction response
     * @throws ResourceNotFoundException if the transaction or newly specified category is not found
     */
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUser(id,currentUser).orElseThrow(()->new ResourceNotFoundException("Transaction", "id", id));

        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            List<Category> categories = categoryRepository.findAccessibleByName(request.getCategory().trim(), currentUser);
            if (categories.isEmpty()) {
                throw new ResourceNotFoundException("Category", "name", request.getCategory());
            }
            Category category = categories.get(0);
            transaction.setCategory(category);
            transaction.setType(category.getType());
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    /**
     * Deletes a transaction owned by the current user.
     *
     * @param id the ID of the transaction to delete
     * @throws ResourceNotFoundException if the transaction is not found
     */
    @Transactional
    public void deleteTransaction(Long id) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUser(id,currentUser).orElseThrow(()->new ResourceNotFoundException("Transaction","id",id));

        transactionRepository.delete(transaction);
    }

    //Helpers 
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .type(transaction.getType().name())
                .category(transaction.getCategory().getName())
                .build();
    }
}
