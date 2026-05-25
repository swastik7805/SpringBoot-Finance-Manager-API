package com.example.syfe.repositories;

import com.example.syfe.models.Goal;
import com.example.syfe.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    //Find all goals for a specific user.
    List<Goal> findAllByUserOrderByTargetDateAsc(User user);

    //Find a specific goal ensuring it belongs to the given user (data isolation).
    Optional<Goal> findByIdAndUser(Long id, User user);

    //Check if a goal with the same name already exists for the user.
    boolean existsByGoalNameAndUser(String goalName, User user);
}
