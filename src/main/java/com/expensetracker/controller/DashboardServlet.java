package com.expensetracker.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.util.GsonProvider;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ExpenseService expenseService;
    private Gson gson;

    @Override
    public void init() {
        expenseService = new ExpenseService();
        gson = GsonProvider.getGson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session == null || session.getAttribute("user") == null) {
            result.put("success", false);
            result.put("error", "Not authenticated");
            response.setStatus(401);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        User user = (User) session.getAttribute("user");
        int userId = user.getId();

        // Fetch dashboard data via service layer
        double totalExpense = expenseService.getTotalExpense(userId);
        List<Expense> recentExpenses = expenseService.getRecentExpenses(userId, 5);
        double highestExpense = expenseService.getHighestExpense(userId);
        double lowestExpense = expenseService.getLowestExpense(userId);

        result.put("success", true);
        result.put("totalExpense", totalExpense);
        result.put("recentExpenses", recentExpenses);
        result.put("highestExpense", highestExpense);
        result.put("lowestExpense", lowestExpense);
        result.put("user", user);
        response.getWriter().write(gson.toJson(result));
    }
}