package com.expensetracker.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.expensetracker.dto.ExpenseDTO;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.ExpenseService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/expenses")
public class ExpensesPageServlet extends HttpServlet {
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

        String action = request.getParameter("action");
        String keyword = request.getParameter("keyword");
        String reportType = request.getParameter("reportType");
        String categoryFilter = request.getParameter("category");
        String monthStr = request.getParameter("month");
        String yearStr = request.getParameter("year");

        List<ExpenseDTO> expenses;
        double reportTotal = 0;
        double reportHighest = 0;
        double reportLowest = 0;
        String activeTab = "list";

        if ("search".equals(action) && keyword != null && !keyword.trim().isEmpty()) {
            expenses = expenseService.searchExpenseDTOs(userId, keyword.trim());
            request.setAttribute("searchKeyword", keyword.trim());
            activeTab = "list";
        } else if ("report".equals(action)) {
            expenses = expenseService.getReportExpenses(userId, reportType, categoryFilter, monthStr, yearStr);
            reportTotal = expenses.stream().mapToDouble(ExpenseDTO::getAmount).sum();
            reportHighest = expenses.stream().mapToDouble(ExpenseDTO::getAmount).max().orElse(0);
            reportLowest = expenses.stream().mapToDouble(ExpenseDTO::getAmount).min().orElse(0);
            activeTab = "report";
        } else {
            expenses = expenseService.getExpenseDTOsByUserId(userId);
        }

        // Check for edit mode
        String editId = request.getParameter("edit");
        if (editId != null) {
            try {
                ExpenseDTO editExpense = expenseService.getExpenseDTOById(Integer.parseInt(editId), userId);
                request.setAttribute("editExpense", editExpense);
            } catch (NumberFormatException e) {
                // ignore invalid edit id
            }
        }

        request.setAttribute("expenses", expenses);
        request.setAttribute("reportTotal", reportTotal);
        request.setAttribute("reportHighest", reportHighest);
        request.setAttribute("reportLowest", reportLowest);
        request.setAttribute("activeTab", activeTab);
        request.setAttribute("reportType", reportType);
        request.setAttribute("categoryFilter", categoryFilter);
        request.setAttribute("monthStr", monthStr);
        request.setAttribute("yearStr", yearStr);

        request.getRequestDispatcher("/WEB-INF/views/expenses.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        int userId = user.getId();

        String action = request.getParameter("action");

        if ("add".equals(action) || "edit".equals(action)) {
            String title = request.getParameter("title");
            String amountStr = request.getParameter("amount");
            String category = request.getParameter("category");
            String dateStr = request.getParameter("expenseDate");
            String description = request.getParameter("description");

            String validationError = expenseService.validateExpense(title, amountStr, category, dateStr);
            if (validationError != null) {
                request.setAttribute("error", validationError);
                doGet(request, response);
                return;
            }

            if ("add".equals(action)) {
                Expense expense = new Expense();
                expense.setTitle(title.trim());
                expense.setAmount(Double.parseDouble(amountStr));
                expense.setCategory(category);
                expense.setExpenseDate(LocalDate.parse(dateStr));
                expense.setDescription(description != null ? description.trim() : "");
                expense.setUserId(userId);
                expenseService.addExpense(expense);
                request.setAttribute("success", "Expense added successfully!");
            } else {
                String idParam = request.getParameter("id");
                if (idParam != null) {
                    Expense expense = new Expense();
                    expense.setId(Integer.parseInt(idParam));
                    expense.setTitle(title.trim());
                    expense.setAmount(Double.parseDouble(amountStr));
                    expense.setCategory(category);
                    expense.setExpenseDate(LocalDate.parse(dateStr));
                    expense.setDescription(description != null ? description.trim() : "");
                    expense.setUserId(userId);
                    expenseService.updateExpense(expense);
                    request.setAttribute("success", "Expense updated successfully!");
                }
            }
        } else if ("delete".equals(action)) {
            String idParam = request.getParameter("id");
            if (idParam != null) {
                expenseService.deleteExpense(Integer.parseInt(idParam), userId);
                request.setAttribute("success", "Expense deleted successfully!");
            }
        }

        doGet(request, response);
    }
}