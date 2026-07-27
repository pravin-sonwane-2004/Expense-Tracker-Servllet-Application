<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<!DOCTYPE html>
	<html lang="en">

	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<title>Register - Expense Tracker</title>
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
	</head>

	<body>
		<div class="auth-container">
			<div class="auth-card">
				<h1>Expense Tracker</h1>
				<h2>Register</h2>

				<% if (request.getAttribute("error") !=null) { %>
					<div class="alert alert-error">
						<%= request.getAttribute("error") %>
					</div>
					<% } %>
						<% if (request.getAttribute("message") !=null) { %>
							<div class="alert alert-success">
								<%= request.getAttribute("message") %>
							</div>
							<% } %>

								<form action="${pageContext.request.contextPath}/api/register" method="post">
									<div class="form-group">
										<label for="name">Name:</label>
										<input type="text" id="name" name="name" required>
									</div>
									<div class="form-group">
										<label for="email">Email:</label>
										<input type="email" id="email" name="email" required>
									</div>
									<div class="form-group">
										<label for="password">Password:</label>
										<input type="password" id="password" name="password" required>
									</div>
									<div class="form-group">
										<label for="confirmPassword">Confirm Password:</label>
										<input type="password" id="confirmPassword" name="confirmPassword" required>
									</div>
									<button type="submit" class="btn btn-primary btn-full">Register</button>
								</form>
								<p class="auth-link">Already have an account? <a
										href="${pageContext.request.contextPath}/login">Login</a></p>
			</div>
		</div>
	</body>

	</html>