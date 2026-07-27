package com.expensetracker.service;

import com.expensetracker.dto.UserDTO;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.impl.UserRepositoryImpl;

public class UserService {
    private UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepositoryImpl();
    }

    // Dependency injection constructor for testability
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    public User getUserById(int userId) {
        return userRepository.findById(userId);
    }

    public UserDTO getUserDTOById(int userId) {
        User user = userRepository.findById(userId);
        return user != null ? convertToDTO(user) : null;
    }

    public boolean isEmailExists(String email) {
        return userRepository.isEmailExists(email);
    }

    public void updateUser(User user) {
        userRepository.update(user);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        return userRepository.changePassword(userId, oldPassword, newPassword);
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
        if (userRepository.isEmailExists(email)) {
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

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}