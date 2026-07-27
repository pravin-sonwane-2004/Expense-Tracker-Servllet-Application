package com.expensetracker.controller;

import java.io.IOException;

import com.expensetracker.model.User;
import com.expensetracker.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/profile")
public class ProfilePageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        String action = request.getParameter("action");

        if ("updateProfile".equals(action)) {
            String name = request.getParameter("name");
            String email = request.getParameter("email");

            String validationError = userService.validateProfileUpdate(name, email);
            if (validationError != null) {
                request.setAttribute("error", validationError);
                request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
                return;
            }

            User user = new User();
            user.setId(sessionUser.getId());
            user.setName(name.trim());
            user.setEmail(email.trim().toLowerCase());
            userService.updateUser(user);

            sessionUser.setName(name.trim());
            sessionUser.setEmail(email.trim().toLowerCase());
            session.setAttribute("user", sessionUser);

            request.setAttribute("success", "Profile updated successfully!");
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);

        } else if ("changePassword".equals(action)) {
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            String validationError = userService.validatePasswordChange(currentPassword, newPassword, confirmPassword);
            if (validationError != null) {
                request.setAttribute("error", validationError);
                request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
                return;
            }

            boolean changed = userService.changePassword(sessionUser.getId(), currentPassword, newPassword);
            if (changed) {
                request.setAttribute("success", "Password changed successfully!");
            } else {
                request.setAttribute("error", "Current password is incorrect");
            }
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        }
    }
}