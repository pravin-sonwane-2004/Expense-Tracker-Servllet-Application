package com.expensetracker.service;

import com.expensetracker.dao.UserDAO;
import com.expensetracker.model.User;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public void registerUser(User user) {
        userDAO.registerUser(user);
    }

    public User loginUser(String email, String password) {
        return userDAO.loginUser(email, password);
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public boolean isEmailExists(String email) {
        return userDAO.isEmailExists(email);
    }

    public void updateUser(User user) {
        userDAO.updateUser(user);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        return userDAO.changePassword(userId, oldPassword, newPassword);
    }

    public String validateRegistration(String name, String email, String password, String confirmPassword) {
        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Invalid email format";
        }
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        if (userDAO.isEmailExists(email)) {
            return "Email already registered";
        }
        return null;
    }

    public String validateLogin(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return "Email and password are required";
        }
        return null;
    }

    public String validateProfileUpdate(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Invalid email format";
        }
        return null;
    }

    public String validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isEmpty()) {
            return "Current password is required";
        }
        if (newPassword == null || newPassword.length() < 6) {
            return "New password must be at least 6 characters";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        return null;
    }
}