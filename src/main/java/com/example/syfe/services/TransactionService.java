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

    //Fetches all transactions for the logged-in user with optional filters.
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(LocalDate startDate,LocalDate endDate,Long categoryId) {
        User currentUser=userService.getCurrentUser();
        return transactionRepository.findAllByUserWithFilters(currentUser,startDate,endDate,categoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

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

    //Deletes a transaction owned by the current user.
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
