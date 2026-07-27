<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String rememberedEmail = (String) request.getAttribute("rememberedEmail");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Expense Tracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-card">
            <h1>Expense Tracker</h1>
            <h2>Login</h2>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error"><%= request.getAttribute("error") %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/api/login" method="post">
                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" 
                           value='<%= rememberedEmail != null ? rememberedEmail : "" %>' required>
                </div>
                <div class="form-group">
                    <label for="password">Password:</label>
                    <input type="password" id="password" name="password" required>
                </div>
                <div class="form-group checkbox-group">
                    <label>
                        <input type="checkbox" name="rememberMe"> Remember me
                    </label>
                </div>
                <button type="submit" class="btn btn-primary btn-full">Login</button>
            </form>
            <p class="auth-link">Don't have an account? <a href="${pageContext.request.contextPath}/register">Register</a></p>
        </div>
    </div>
</body>
</html>