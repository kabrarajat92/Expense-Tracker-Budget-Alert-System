package com.expensetracker.expenseservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.expensetracker.expenseservice.entity.Budget;
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
	List<Budget> findByUsername(String username);
    Optional<Budget> findByUsernameAndCategory(String username, String category);
}
