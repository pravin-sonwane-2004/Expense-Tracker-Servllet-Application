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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
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

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        Map<String, Object> result = new HashMap<>();

        // Validate using service layer
        String validationError = userService.validateLogin(email, password);
        if (validationError != null) {
            result.put("success", false);
            result.put("error", validationError);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        // Authenticate using service layer
        User user = userService.loginUser(email.trim().toLowerCase(), password);
        if (user == null) {
            result.put("success", false);
            result.put("error", "Invalid email or password");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        // Create session
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Handle "Remember Me"
        if ("on".equals(rememberMe)) {
            Cookie cookie = new Cookie("rememberedEmail", email);
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            cookie.setHttpOnly(true);
            cookie.setPath(request.getContextPath() + "/");
            response.addCookie(cookie);
        } else {
            Cookie cookie = new Cookie("rememberedEmail", "");
            cookie.setMaxAge(0);
            cookie.setPath(request.getContextPath() + "/");
            response.addCookie(cookie);
        }

        // Redirect to dashboard on successful login
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
