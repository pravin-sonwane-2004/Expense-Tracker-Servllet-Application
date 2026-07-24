package com.expensetracker.service;

import java.time.LocalDate;
import java.util.List;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Expense;

public class ExpenseService {
    private ExpenseDAO expenseDAO;

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
    }

    public void addExpense(Expense expense) {
        expenseDAO.addExpense(expense);
    }

    public void updateExpense(Expense expense) {
        expenseDAO.updateExpense(expense);
    }

    public void deleteExpense(int expenseId, int userId) {
        expenseDAO.deleteExpense(expenseId, userId);
    }

    public Expense getExpenseById(int expenseId, int userId) {
        return expenseDAO.getExpenseById(expenseId, userId);
    }

    public List<Expense> getExpensesByUserId(int userId) {
        return expenseDAO.getExpensesByUserId(userId);
    }

    public List<Expense> searchExpenses(int userId, String keyword) {
        return expenseDAO.searchExpenses(userId, keyword);
    }

    public double getTotalExpense(int userId) {
        return expenseDAO.getTotalExpense(userId);
    }

    public List<Expense> getRecentExpenses(int userId, int limit) {
        return expenseDAO.getRecentExpenses(userId, limit);
    }

    public double getHighestExpense(int userId) {
        return expenseDAO.getHighestExpense(userId);
    }

    public double getLowestExpense(int userId) {
        return expenseDAO.getLowestExpense(userId);
    }

    public List<Expense> getExpensesByMonth(int userId, int year, int month) {
        return expenseDAO.getExpensesByMonth(userId, year, month);
    }

    public List<Expense> getExpensesByCategory(int userId, String category) {
        return expenseDAO.getExpensesByCategory(userId, category);
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

    public ReportData generateReport(int userId, String reportType, String category, String monthStr, String yearStr) {
        List<Expense> expenses;

        if ("monthly".equals(reportType) && monthStr != null && yearStr != null) {
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);
            expenses = expenseDAO.getExpensesByMonth(userId, year, month);
        } else if ("category".equals(reportType) && category != null && !category.isEmpty()) {
            expenses = expenseDAO.getExpensesByCategory(userId, category);
        } else {
            expenses = expenseDAO.getExpensesByUserId(userId);
        }

        return new ReportData(expenses);
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