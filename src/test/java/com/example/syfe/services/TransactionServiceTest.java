package com.example.syfe.services;

import com.example.syfe.dtos.requests.TransactionRequest;
import com.example.syfe.dtos.responses.TransactionResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.exceptions.BusinessRuleException;
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
import java.util.Optional;
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
        category = Category.builder().id(1L).name("Food").build();
        
        request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Lunch");
        request.setDate(LocalDate.now());
        request.setCategoryId(1L);
        request.setType("EXPENSE");

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
        when(categoryRepository.findAccessibleById(1L, user)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("Food", response.getCategoryName());
    }

    @Test
    void createTransaction_FutureDate() {
        when(userService.getCurrentUser()).thenReturn(user);
        request.setDate(LocalDate.now().plusDays(1));

        assertThrows(BusinessRuleException.class, () -> transactionService.createTransaction(request));
    }
}
