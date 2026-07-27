package com.expensetracker.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/dashboard", "/expenses", "/profile", "/api/dashboard", "/api/expenses", "/api/profile"})
public class AuthFilter extends HttpFilter implements Filter {
    private static final long serialVersionUID = 1L;

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
            // For page requests, redirect to login
            if (path.startsWith("/dashboard") || path.startsWith("/expenses") || path.startsWith("/profile")) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
                return;
            }
            // For API requests, return JSON
            httpResponse.setStatus(401);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"success\":false,\"error\":\"Not authenticated\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}