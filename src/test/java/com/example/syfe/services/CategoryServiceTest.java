package com.example.syfe.services;

import com.example.syfe.dtos.requests.CategoryRequest;
import com.example.syfe.dtos.responses.CategoryResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.exceptions.BusinessRuleException;
import com.example.syfe.exceptions.DuplicateResourceException;
import com.example.syfe.models.Category;
import com.example.syfe.models.User;
import com.example.syfe.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category category;
    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        
        category = Category.builder()
                .id(1L)
                .name("Groceries")
                .type(TransactionType.EXPENSE)
                .isDefault(false)
                .user(user)
                .build();
                
        request = new CategoryRequest();
        request.setName("Groceries");
        request.setType("EXPENSE");
    }

    @Test
    void createCategory_Success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsByNameAndUser("Groceries", user)).thenReturn(false);
        when(categoryRepository.existsByNameAndTypeAndIsDefaultTrue("Groceries", TransactionType.EXPENSE)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Groceries", response.getName());
        assertEquals("EXPENSE", response.getType());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateUserCategory() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsByNameAndUser("Groceries", user)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategory_InvalidType() {
        when(userService.getCurrentUser()).thenReturn(user);
        request.setType("INVALID");

        assertThrows(BusinessRuleException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void deleteCategory_Success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByNameAndUserAndIsDefaultFalse("Groceries", user)).thenReturn(Optional.of(category));
        when(categoryRepository.countTransactionsByCategory(category)).thenReturn(0L);

        assertDoesNotThrow(() -> categoryService.deleteCategory("Groceries"));
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void deleteCategory_HasTransactions() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByNameAndUserAndIsDefaultFalse("Groceries", user)).thenReturn(Optional.of(category));
        when(categoryRepository.countTransactionsByCategory(category)).thenReturn(5L);

        assertThrows(BusinessRuleException.class, () -> categoryService.deleteCategory("Groceries"));
        verify(categoryRepository, never()).delete(any());
    }
}
