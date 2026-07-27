package com.expensetracker.controller;

import java.io.IOException;
import java.util.List;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardPageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ExpenseService expenseService;

    @Override
    public void init() {
        expenseService = new ExpenseService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        int userId = user.getId();

        // Fetch all dashboard data via service layer
        double totalExpense = expenseService.getTotalExpense(userId);
        List<ExpenseDTO> recentExpenses = expenseService.getRecentExpenseDTOs(userId, 5);
        double highestExpense = expenseService.getHighestExpense(userId);
        double lowestExpense = expenseService.getLowestExpense(userId);

        // Set attributes for JSP rendering
        request.setAttribute("totalExpense", totalExpense);
        request.setAttribute("recentExpenses", recentExpenses);
        request.setAttribute("highestExpense", highestExpense);
        request.setAttribute("lowestExpense", lowestExpense);

        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}