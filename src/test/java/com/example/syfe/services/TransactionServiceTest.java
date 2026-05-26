package com.example.syfe.services;

import com.example.syfe.dtos.requests.TransactionRequest;
import com.example.syfe.dtos.responses.TransactionResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.exceptions.BusinessRuleException;
import com.example.syfe.exceptions.ResourceNotFoundException;
import com.example.syfe.models.Category;
import com.example.syfe.models.Transaction;
import com.example.syfe.models.User;
import com.example.syfe.repositories.CategoryRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category category;
    private TransactionRequest request;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        // Category now carries the type — no need to pass it in the request
        category = Category.builder().id(1L).name("Food").type(TransactionType.EXPENSE).build();

        // Request now uses category name string (no categoryId / type fields)
        request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Lunch");
        request.setDate(LocalDate.now());
        request.setCategory("Food");

        transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .description("Lunch")
                .date(LocalDate.now())
                .type(TransactionType.EXPENSE)
                .category(category)
                .user(user)
                .build();
    }

    @Test
    void createTransaction_Success() {
        when(userService.getCurrentUser()).thenReturn(user);
        // Service now looks up by name via findAccessibleByName
        when(categoryRepository.findAccessibleByName("Food", user)).thenReturn(List.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        // Response now has a single 'category' field (category name)
        assertEquals("Food", response.getCategory());
        // Type is inferred from the category entity
        assertEquals("EXPENSE", response.getType());
    }

    @Test
    void createTransaction_CategoryNotFound_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findAccessibleByName("Food", user)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_FutureDate_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(user);
        request.setDate(LocalDate.now().plusDays(1));

        assertThrows(BusinessRuleException.class, () -> transactionService.createTransaction(request));
    }
}
