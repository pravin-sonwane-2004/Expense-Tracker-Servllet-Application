package com.expensetracker.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class AppListener implements ServletContextListener, HttpSessionListener {

    private int activeSessions = 0;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Expense Tracker Application Started ===");
        System.out.println("Server Info: " + sce.getServletContext().getServerInfo());
        System.out.println("Context Path: " + sce.getServletContext().getContextPath());

        // Set application-wide attributes
        sce.getServletContext().setAttribute("appName", "Expense Tracker");
        sce.getServletContext().setAttribute("appVersion", "1.0.0");
        sce.getServletContext().setAttribute("activeSessions", activeSessions);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== Expense Tracker Application Shutting Down ===");
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions++;
        se.getSession().getServletContext().setAttribute("activeSessions", activeSessions);
        System.out.println("Session created. Active sessions: " + activeSessions);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions--;
        se.getSession().getServletContext().setAttribute("activeSessions", activeSessions);
        System.out.println("Session destroyed. Active sessions: " + activeSessions);
    }
}