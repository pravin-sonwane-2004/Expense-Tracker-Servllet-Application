package com.expensetracker.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.expensetracker.model.User;
import com.expensetracker.service.UserService;
import com.expensetracker.util.GsonProvider;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private Gson gson;

    @Override
    public void init() {
        userService = new UserService();
        gson = GsonProvider.getGson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        Map<String, Object> result = new HashMap<>();

        // Validate using service layer
        String validationError = userService.validateRegistration(name, email, password, confirmPassword);
        if (validationError != null) {
            result.put("success", false);
            result.put("error", validationError);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        // Create and register user
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(password);
        userService.registerUser(user);

        // Redirect to login page on successful registration
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
