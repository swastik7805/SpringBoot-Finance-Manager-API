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

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    // Creates a new transaction with full validation
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = userService.getCurrentUser();
        TransactionType type = parseTransactionType(request.getType());

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Transaction date cannot be in the future");
        }

        Category category=categoryRepository.findAccessibleById(request.getCategoryId(),currentUser).orElseThrow(()->new ResourceNotFoundException("Category","id",request.getCategoryId()));

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

    //Fetches all transactions for the logged-in user with optional filters.
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(LocalDate startDate,LocalDate endDate,Long categoryId) {
        User currentUser=userService.getCurrentUser();
        return transactionRepository.findAllByUserWithFilters(currentUser,startDate,endDate,categoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    //Updates all fields of a transaction except the date.
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request) {
        User currentUser = userService.getCurrentUser();
        TransactionType type = parseTransactionType(request.getType());

        Transaction transaction = transactionRepository.findByIdAndUser(id,currentUser).orElseThrow(()->new ResourceNotFoundException("Transaction", "id", id));

        // Validate: category must exist and be accessible to this user
        Category category=categoryRepository.findAccessibleById(request.getCategoryId(),currentUser).orElseThrow(()->new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setType(type);
        transaction.setCategory(category);

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    //Deletes a transaction owned by the current user.
    @Transactional
    public void deleteTransaction(Long id) {
        User currentUser = userService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUser(id,currentUser).orElseThrow(()->new ResourceNotFoundException("Transaction","id",id));

        transactionRepository.delete(transaction);
    }

    //Helpers 
    private TransactionType parseTransactionType(String type) {
        try
        {
            return TransactionType.valueOf(type.trim().toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid transaction type: '"+type+"'. Must be INCOME or EXPENSE");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .type(transaction.getType().name())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
