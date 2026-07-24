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
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/profile")
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private Gson gson;

    @Override
    public void init() {
        userService = new UserService();
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();

        if (!isAuthenticated(request, response, result)) {
			return;
		}

        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        result.put("success", true);
        result.put("user", user);
        response.getWriter().write(gson.toJson(result));
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

        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute("user");
        String action = request.getParameter("action");

        if ("updateProfile".equals(action)) {
            handleUpdateProfile(request, response, sessionUser, session);
        } else if ("changePassword".equals(action)) {
            handleChangePassword(request, response, sessionUser);
        } else {
            result.put("success", false);
            result.put("error", "Invalid action");
            response.getWriter().write(gson.toJson(result));
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response,
                                      User sessionUser, HttpSession session) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String name = request.getParameter("name");
        String email = request.getParameter("email");

        // Validate using service layer
        String validationError = userService.validateProfileUpdate(name, email);
        if (validationError != null) {
            result.put("success", false);
            result.put("error", validationError);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        // Update user via service layer
        User user = new User();
        user.setId(sessionUser.getId());
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        userService.updateUser(user);

        // Update session
        sessionUser.setName(name.trim());
        sessionUser.setEmail(email.trim().toLowerCase());
        session.setAttribute("user", sessionUser);

        result.put("success", true);
        result.put("message", "Profile updated successfully");
        result.put("user", sessionUser);
        response.getWriter().write(gson.toJson(result));
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response,
                                       User sessionUser) throws IOException {
        Map<String, Object> result = new HashMap<>();

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Validate using service layer
        String validationError = userService.validatePasswordChange(currentPassword, newPassword, confirmPassword);
        if (validationError != null) {
            result.put("success", false);
            result.put("error", validationError);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        // Change password via service layer
        boolean changed = userService.changePassword(sessionUser.getId(), currentPassword, newPassword);
        if (changed) {
            result.put("success", true);
            result.put("message", "Password changed successfully");
        } else {
            result.put("success", false);
            result.put("error", "Current password is incorrect");
        }
        response.getWriter().write(gson.toJson(result));
    }
}