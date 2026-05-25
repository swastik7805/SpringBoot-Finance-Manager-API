package com.example.syfe.repositories;

import com.example.syfe.enums.TransactionType;
import com.example.syfe.models.Category;
import com.example.syfe.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM Category AS c WHERE c.isDefault=true OR c.user=:user ORDER BY c.isDefault DESC,c.name ASC")
    List<Category> findAllAccessibleByUser(@Param("user") User user);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.type = :type AND (c.isDefault = true OR c.user = :user)")
    Optional<Category> findAccessibleByNameAndType(
            @Param("name") String name,
            @Param("type") TransactionType type,
            @Param("user") User user);

    Optional<Category> findByNameAndUserAndIsDefaultFalse(String name, User user);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.isDefault = true OR c.user = :user)")
    Optional<Category> findAccessibleById(@Param("id") Long id, @Param("user") User user);
    
    boolean existsByNameAndUser(String name, User user);
    boolean existsByNameAndTypeAndIsDefaultTrue(String name, TransactionType type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.category = :category")
    long countTransactionsByCategory(@Param("category") Category category);
}
