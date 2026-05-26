package com.example.syfe.services;

import com.example.syfe.dtos.requests.CategoryRequest;
import com.example.syfe.dtos.responses.CategoryResponse;
import com.example.syfe.enums.TransactionType;
import com.example.syfe.exceptions.BusinessRuleException;
import com.example.syfe.exceptions.DuplicateResourceException;
import com.example.syfe.exceptions.ResourceNotFoundException;
import com.example.syfe.models.Category;
import com.example.syfe.models.User;
import com.example.syfe.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    //Creates a custom, user-specific category.
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User currentUser = userService.getCurrentUser();
        TransactionType type = parseTransactionType(request.getType());

        if(categoryRepository.existsByNameAndUser(request.getName().trim(), currentUser))
        {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }
        
        if(categoryRepository.existsByNameAndTypeAndIsDefaultTrue(request.getName().trim(), type))
        {
            throw new DuplicateResourceException("A default category with name '" + request.getName() + "' and type '" + type + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .type(type)
                .isDefault(false)
                .user(currentUser)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    //Returns all categories accessible to the logged-in user
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        User currentUser = userService.getCurrentUser();
        return categoryRepository.findAllAccessibleByUser(currentUser)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Deletes a user's custom category by name.
    @Transactional
    public void deleteCategory(String name) {
        User currentUser=userService.getCurrentUser();

        Category category=categoryRepository.findByNameAndUserAndIsDefaultFalse(name.trim(),currentUser).orElseThrow(()->new ResourceNotFoundException("Custom category", "name", name));

        if (category.isDefault())
        {
            throw new BusinessRuleException("Default categories cannot be deleted");
        }

        long transactionCount=categoryRepository.countTransactionsByCategory(category);
        if (transactionCount>0)
        {
            throw new BusinessRuleException("Cannot delete category '"+name+"' because it is referenced by "+transactionCount+" transaction(s). Remove or reassign them first.");
        }

        categoryRepository.delete(category);
    }

    // Helpers 
    private TransactionType parseTransactionType(String type) {
        try
        {
            return TransactionType.valueOf(type.trim().toUpperCase());
        }
        catch(IllegalArgumentException e)
        {
            throw new BusinessRuleException("Invalid category type:" +type+ ". Must be INCOME or EXPENSE");
        }
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType().name())
                .isCustom(!category.isDefault())
                .build();
    }
}
