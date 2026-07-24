package com.expensetracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.expensetracker.exception.DBException;
import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;

public class ExpenseDAO {

    public void addExpense(Expense expense) {
        String sql = "INSERT INTO expenses (title, amount, category, expense_date, description, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setDate(4, java.sql.Date.valueOf(expense.getExpenseDate()));
            ps.setString(5, expense.getDescription());
            ps.setInt(6, expense.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("Failed to add expense", e);
        }
    }

    public void updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET title = ?, amount = ?, category = ?, expense_date = ?, description = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setDate(4, java.sql.Date.valueOf(expense.getExpenseDate()));
            ps.setString(5, expense.getDescription());
            ps.setInt(6, expense.getId());
            ps.setInt(7, expense.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("Failed to update expense", e);
        }
    }

    public void deleteExpense(int expenseId, int userId) {
        String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("Failed to delete expense", e);
        }
    }

    public Expense getExpenseById(int expenseId, int userId) {
        String sql = "SELECT * FROM expenses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapExpense(rs);
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get expense by ID", e);
        }
        return null;
    }

    public List<Expense> getExpensesByUserId(int userId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get expenses by user ID", e);
        }
        return expenses;
    }

    public List<Expense> searchExpenses(int userId, String keyword) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND (title LIKE ? OR category LIKE ? OR description LIKE ?) ORDER BY expense_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            String pattern = "%" + keyword + "%";
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to search expenses", e);
        }
        return expenses;
    }

    public double getTotalIncome(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ? AND amount > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get total income", e);
        }
        return 0;
    }

    public double getTotalExpense(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ? AND amount > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get total expense", e);
        }
        return 0;
    }

    public List<Expense> getRecentExpenses(int userId, int limit) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get recent expenses", e);
        }
        return expenses;
    }

    public List<Expense> getExpensesByMonth(int userId, int year, int month) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND YEAR(expense_date) = ? AND MONTH(expense_date) = ? ORDER BY expense_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get expenses by month", e);
        }
        return expenses;
    }

    public List<Expense> getExpensesByCategory(int userId, String category) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND category = ? ORDER BY expense_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get expenses by category", e);
        }
        return expenses;
    }

    public double getHighestExpense(int userId) {
        String sql = "SELECT COALESCE(MAX(amount), 0) FROM expenses WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get highest expense", e);
        }
        return 0;
    }

    public double getLowestExpense(int userId) {
        String sql = "SELECT COALESCE(MIN(amount), 0) FROM expenses WHERE user_id = ? AND amount > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get lowest expense", e);
        }
        return 0;
    }

    private Expense mapExpense(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setTitle(rs.getString("title"));
        expense.setAmount(rs.getDouble("amount"));
        expense.setCategory(rs.getString("category"));
        expense.setExpenseDate(rs.getObject("expense_date", LocalDate.class));
        expense.setDescription(rs.getString("description"));
        expense.setUserId(rs.getInt("user_id"));
        return expense;
    }
}