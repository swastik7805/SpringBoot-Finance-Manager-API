package com.example.syfe.repositories;

import com.example.syfe.models.Transaction;
import com.example.syfe.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    //Find a transaction by ID ensuring it belongs to the given user (data isolation).
    Optional<Transaction> findByIdAndUser(Long id, User user);

    //Fetch all transactions for a user with optional filters, sorted newest first.
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user = :user
              AND (cast(:startDate as date) IS NULL OR t.date >= :startDate)
              AND (cast(:endDate as date) IS NULL OR t.date <= :endDate)
              AND (cast(:categoryId as long) IS NULL OR t.category.id = :categoryId)
            ORDER BY t.date DESC, t.createdAt DESC
            """)
    List<Transaction> findAllByUserWithFilters(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryId") Long categoryId);

    //Find all transactions for a user within a date range (used by reports/goals).
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate AND t.date <= :endDate ORDER BY t.date DESC")
    List<Transaction> findByUserAndDateBetween(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
