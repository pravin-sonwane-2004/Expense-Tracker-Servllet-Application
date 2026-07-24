package com.expensetracker.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.expensetracker.util.GsonProvider;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Gson gson;

    @Override
    public void init() {
        gson = GsonProvider.getGson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        // Clear any cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberedEmail".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath(request.getContextPath() + "/");
                    response.addCookie(cookie);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        response.getWriter().write(gson.toJson(result));
    }
}