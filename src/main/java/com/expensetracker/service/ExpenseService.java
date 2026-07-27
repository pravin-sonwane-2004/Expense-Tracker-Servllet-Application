package com.expensetracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.impl.ExpenseRepositoryImpl;

public class ExpenseService {
    private ExpenseRepository expenseRepository;

    public ExpenseService() {
        this.expenseRepository = new ExpenseRepositoryImpl();
    }

    // Dependency injection constructor for testability
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public void addExpense(Expense expense) {
        expenseRepository.save(expense);
    }

    public void updateExpense(Expense expense) {
        expenseRepository.update(expense);
    }

    public void deleteExpense(int expenseId, int userId) {
        expenseRepository.delete(expenseId, userId);
    }

    public Expense getExpenseById(int expenseId, int userId) {
        return expenseRepository.findById(expenseId, userId);
    }

    public ExpenseDTO getExpenseDTOById(int expenseId, int userId) {
        Expense expense = expenseRepository.findById(expenseId, userId);
        return expense != null ? convertToDTO(expense) : null;
    }

    public List<Expense> getExpensesByUserId(int userId) {
        return expenseRepository.findByUserId(userId);
    }

    public List<ExpenseDTO> getExpenseDTOsByUserId(int userId) {
        return expenseRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Expense> searchExpenses(int userId, String keyword) {
        return expenseRepository.search(userId, keyword);
    }

    public List<ExpenseDTO> searchExpenseDTOs(int userId, String keyword) {
        return expenseRepository.search(userId, keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public double getTotalExpense(int userId) {
        return expenseRepository.getTotalExpense(userId);
    }

    public List<Expense> getRecentExpenses(int userId, int limit) {
        return expenseRepository.findRecent(userId, limit);
    }

    public List<ExpenseDTO> getRecentExpenseDTOs(int userId, int limit) {
        return expenseRepository.findRecent(userId, limit).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public double getHighestExpense(int userId) {
        return expenseRepository.getHighestExpense(userId);
    }

    public double getLowestExpense(int userId) {
        return expenseRepository.getLowestExpense(userId);
    }

    public List<Expense> getExpensesByMonth(int userId, int year, int month) {
        return expenseRepository.findByMonth(userId, year, month);
    }

    public List<ExpenseDTO> getExpenseDTOsByMonth(int userId, int year, int month) {
        return expenseRepository.findByMonth(userId, year, month).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Expense> getExpensesByCategory(int userId, String category) {
        return expenseRepository.findByCategory(userId, category);
    }

    public List<ExpenseDTO> getExpenseDTOsByCategory(int userId, String category) {
        return expenseRepository.findByCategory(userId, category).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String validateExpense(String title, String amountStr, String category, String dateStr) {
        if (title == null || title.trim().isEmpty()) {
            return "Title is required";
        }
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                return "Amount must be greater than zero";
            }
        } catch (NumberFormatException e) {
            return "Invalid amount";
        }
        if (category == null || category.trim().isEmpty()) {
            return "Category is required";
        }
        try {
            LocalDate.parse(dateStr);
        } catch (Exception e) {
            return "Invalid date format";
        }
        return null;
    }

    public List<ExpenseDTO> getReportExpenses(int userId, String reportType, String category, String monthStr, String yearStr) {
        if ("monthly".equals(reportType) && monthStr != null && yearStr != null) {
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);
            return getExpenseDTOsByMonth(userId, year, month);
        } else if ("category".equals(reportType) && category != null && !category.isEmpty()) {
            return getExpenseDTOsByCategory(userId, category);
        } else {
            return getExpenseDTOsByUserId(userId);
        }
    }

    private ExpenseDTO convertToDTO(Expense expense) {
        return new ExpenseDTO(
            expense.getId(),
            expense.getTitle(),
            expense.getAmount(),
            expense.getCategory(),
            expense.getExpenseDate(),
            expense.getDescription(),
            expense.getUserId()
        );
    }

    public static class ReportData {
        private List<Expense> expenses;
        private double totalAmount;
        private double highestAmount;
        private double lowestAmount;

        public ReportData(List<Expense> expenses) {
            this.expenses = expenses;
            this.totalAmount = 0;
            this.highestAmount = 0;
            this.lowestAmount = Double.MAX_VALUE;

            if (expenses != null && !expenses.isEmpty()) {
                for (Expense e : expenses) {
                    totalAmount += e.getAmount();
                    if (e.getAmount() > highestAmount) {
                        highestAmount = e.getAmount();
                    }
                    if (e.getAmount() < lowestAmount) {
                        lowestAmount = e.getAmount();
                    }
                }
            } else {
                lowestAmount = 0;
            }
        }

        public List<Expense> getExpenses() { return expenses; }
        public double getTotalAmount() { return totalAmount; }
        public double getHighestAmount() { return highestAmount; }
        public double getLowestAmount() { return lowestAmount == Double.MAX_VALUE ? 0 : lowestAmount; }
    }
}