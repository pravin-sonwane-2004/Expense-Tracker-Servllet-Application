package com.expensetracker.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.expensetracker.exception.DBException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.util.DBConnection;

public class ExpenseRepositoryImpl implements ExpenseRepository {

    @Override
    public void save(Expense expense) {
        String sql = "INSERT INTO expenses (title, amount, category, expense_date, description, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setObject(4, expense.getExpenseDate());
            ps.setString(5, expense.getDescription());
            ps.setInt(6, expense.getUserId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    expense.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to add expense", e);
        }
    }

    @Override
    public void update(Expense expense) {
        String sql = "UPDATE expenses SET title = ?, amount = ?, category = ?, expense_date = ?, description = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setObject(4, expense.getExpenseDate());
            ps.setString(5, expense.getDescription());
            ps.setInt(6, expense.getId());
            ps.setInt(7, expense.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DBException("Failed to update expense", e);
        }
    }

    @Override
    public void delete(int expenseId, int userId) {
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

    @Override
    public Expense findById(int expenseId, int userId) {
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

    @Override
    public List<Expense> findByUserId(int userId) {
        String sql = "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC";
        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    expenses.add(mapExpense(rs));
                }
            }
        } catch (SQLException e) {
            throw new DBException("Failed to get expenses", e);
        }
        return expenses;
    }

    @Override
    public List<Expense> search(int userId, String keyword) {
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND (title LIKE ? OR category LIKE ? OR description LIKE ?) ORDER BY expense_date DESC, id DESC";
        List<Expense> expenses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setInt(1, userId);
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

    @Override
    public double getTotalExpense(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE user_id = ?";
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

    @Override
    public List<Expense> findRecent(int userId, int limit) {
        String sql = "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC LIMIT ?";
        List<Expense> expenses = new ArrayList<>();
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

    @Override
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

    @Override
    public double getLowestExpense(int userId) {
        String sql = "SELECT COALESCE(MIN(amount), 0) FROM expenses WHERE user_id = ?";
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

    @Override
    public List<Expense> findByMonth(int userId, int year, int month) {
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND YEAR(expense_date) = ? AND MONTH(expense_date) = ? ORDER BY expense_date DESC, id DESC";
        List<Expense> expenses = new ArrayList<>();
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

    @Override
    public List<Expense> findByCategory(int userId, String category) {
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND category = ? ORDER BY expense_date DESC, id DESC";
        List<Expense> expenses = new ArrayList<>();
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