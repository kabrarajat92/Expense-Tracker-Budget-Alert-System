package com.expensetracker.budgetservice.repository;

import com.expensetracker.budgetservice.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUsername(String username);
    Optional<Budget> findByUsernameAndCategoryAndMonthYear(String username, String category, String monthYear);
}
