package com.expensetracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.ExpenseService.ReportData;
import com.expensetracker.util.GsonProvider;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/expenses")
public class ExpenseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ExpenseService expenseService;
    private Gson gson;

    @Override
    public void init() {
        expenseService = new ExpenseService();
        gson = GsonProvider.getGson();
    }

    private boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response, Map<String, Object> result) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            result.put("success", false);
            result.put("error", "Not authenticated");
            response.setStatus(401);
            response.getWriter().write(gson.toJson(result));
            return false;
        }
        return true;
    }

    private int getUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        return user.getId();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        if (!isAuthenticated(request, response, result)) {
			return;
		}

        int userId = getUserId(request);
        String action = request.getParameter("action");
        if (action == null) {
			action = "list";
		}

        switch (action) {
            case "edit":
                handleGetExpense(request, response, userId);
                break;
            case "search":
                handleSearchExpenses(request, response, userId);
                break;
            case "report":
                handleReport(request, response, userId);
                break;
            default:
                handleListExpenses(response, userId);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        if (!isAuthenticated(request, response, result)) {
			return;
		}

        int userId = getUserId(request);
        String action = request.getParameter("action");
        if (action == null) {
			action = "list";
		}

        switch (action) {
            case "add":
                handleAddExpense(request, response, userId);
                break;
            case "edit":
                handleUpdateExpense(request, response, userId);
                break;
            case "delete":
                handleDeleteExpense(request, response, userId);
                break;
            default:
                handleListExpenses(response, userId);
                break;
        }
    }

    private void handleListExpenses(HttpServletResponse response, int userId) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<Expense> expenses = expenseService.getExpensesByUserId(userId);
        result.put("success", true);
        result.put("expenses", expenses);
        response.getWriter().write(gson.toJson(result));
    }

    private void handleGetExpense(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String idParam = request.getParameter("id");
        if (idParam == null) {
            result.put("success", false);
            result.put("error", "Expense ID required");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        int expenseId = Integer.parseInt(idParam);
        Expense expense = expenseService.getExpenseById(expenseId, userId);
        if (expense != null) {
            result.put("success", true);
            result.put("expense", expense);
        } else {
            result.put("success", false);
            result.put("error", "Expense not found");
        }
        response.getWriter().write(gson.toJson(result));
    }

    private void handleAddExpense(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String title = request.getParameter("title");
        String amountStr = request.getParameter("amount");
        String category = request.getParameter("category");
        String dateStr = request.getParameter("expenseDate");
        String description = request.getParameter("description");

        // Validate using service layer
        String validationError = expenseService.validateExpense(title, amountStr, category, dateStr);
        if (validationError != null) {
            result.put("success", false);
            result.put("error", validationError);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        Expense expense = new Expense();
        expense.setTitle(title.trim());
        expense.setAmount(Double.parseDouble(amountStr));
        expense.setCategory(category);
        expense.setExpenseDate(LocalDate.parse(dateStr));
        expense.setDescription(description != null ? description.trim() : "");
        expense.setUserId(userId);

        expenseService.addExpense(expense);
        result.put("success", true);
        result.put("message", "Expense added successfully");
        response.getWriter().write(gson.toJson(result));
    }

    private void handleUpdateExpense(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String idParam = request.getParameter("id");
        if (idParam == null) {
            result.put("success", false);
            result.put("error", "Invalid expense ID");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        int expenseId = Integer.parseInt(idParam);
        String title = request.getParameter("title");
        String amountStr = request.getParameter("amount");
        String category = request.getParameter("category");
        String dateStr = request.getParameter("expenseDate");
        String description = request.getParameter("description");

        // Validate using service layer
        String validationError = expenseService.validateExpense(title, amountStr, category, dateStr);
        if (validationError != null) {
            result.put("success", false);
            result.put("error", validationError);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        Expense expense = new Expense();
        expense.setId(expenseId);
        expense.setTitle(title.trim());
        expense.setAmount(Double.parseDouble(amountStr));
        expense.setCategory(category);
        expense.setExpenseDate(LocalDate.parse(dateStr));
        expense.setDescription(description != null ? description.trim() : "");
        expense.setUserId(userId);

        expenseService.updateExpense(expense);
        result.put("success", true);
        result.put("message", "Expense updated successfully");
        response.getWriter().write(gson.toJson(result));
    }

    private void handleDeleteExpense(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String idParam = request.getParameter("id");
        if (idParam == null) {
            result.put("success", false);
            result.put("error", "Expense ID required");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        int expenseId = Integer.parseInt(idParam);
        expenseService.deleteExpense(expenseId, userId);
        result.put("success", true);
        result.put("message", "Expense deleted successfully");
        response.getWriter().write(gson.toJson(result));
    }

    private void handleSearchExpenses(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String keyword = request.getParameter("keyword");
        List<Expense> expenses;

        if (keyword != null && !keyword.trim().isEmpty()) {
            expenses = expenseService.searchExpenses(userId, keyword.trim());
            result.put("searchKeyword", keyword);
        } else {
            expenses = expenseService.getExpensesByUserId(userId);
        }

        result.put("success", true);
        result.put("expenses", expenses);
        response.getWriter().write(gson.toJson(result));
    }

    private void handleReport(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String reportType = request.getParameter("reportType");
        String category = request.getParameter("category");
        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("year");

        // Generate report using service layer
        ReportData reportData = expenseService.generateReport(userId, reportType, category, monthStr, yearStr);

        result.put("success", true);
        result.put("reportExpenses", reportData.getExpenses());
        result.put("reportTotal", reportData.getTotalAmount());
        result.put("reportHighest", reportData.getHighestAmount());
        result.put("reportLowest", reportData.getLowestAmount());
        response.getWriter().write(gson.toJson(result));
    }
}