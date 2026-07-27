package com.expensetracker.repository;

import com.expensetracker.model.User;

public interface UserRepository {
    void save(User user);
    User findByEmailAndPassword(String email, String password);
    User findById(int userId);
    boolean isEmailExists(String email);
    void update(User user);
    boolean changePassword(int userId, String oldPassword, String newPassword);
}