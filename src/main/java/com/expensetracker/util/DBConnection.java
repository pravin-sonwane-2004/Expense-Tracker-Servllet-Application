package com.expensetracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.expensetracker.exception.DBException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/expense_tracker";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "0000";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DBException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new DBException("Failed to connect to database. Ensure MySQL is running and database 'expense_tracker' exists.", e);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DBException("Failed to close connection", e);
            }
        }
    }
}