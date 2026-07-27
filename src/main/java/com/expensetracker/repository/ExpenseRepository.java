package com.expensetracker.repository;

import java.util.List;

import com.expensetracker.model.Expense;

public interface ExpenseRepository {
    void save(Expense expense);
    void update(Expense expense);
    void delete(int expenseId, int userId);
    Expense findById(int expenseId, int userId);
    List<Expense> findByUserId(int userId);
    List<Expense> search(int userId, String keyword);
    double getTotalExpense(int userId);
    List<Expense> findRecent(int userId, int limit);
    double getHighestExpense(int userId);
    double getLowestExpense(int userId);
    List<Expense> findByMonth(int userId, int year, int month);
    List<Expense> findByCategory(int userId, String category);
}