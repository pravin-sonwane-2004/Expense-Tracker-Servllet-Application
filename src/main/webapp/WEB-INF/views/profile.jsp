<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<%@ page import="com.expensetracker.model.User" %>
		<% String error=(String) request.getAttribute("error"); String success=(String) request.getAttribute("success");
			User user=(User) session.getAttribute("user"); %>
			<!DOCTYPE html>
			<html lang="en">

			<head>
				<meta charset="UTF-8">
				<meta name="viewport" content="width=device-width, initial-scale=1.0">
				<title>Profile - Expense Tracker</title>
				<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
			</head>

			<body>
				<nav class="navbar">
					<div class="nav-container">
						<a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">Expense Tracker</a>
						<ul class="nav-menu">
							<li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
							<li><a href="${pageContext.request.contextPath}/expenses">Expenses</a></li>
							<li><a href="${pageContext.request.contextPath}/profile" class="active">Profile</a></li>
							<li>
								<form action="${pageContext.request.contextPath}/api/logout" method="post"
									style="display:inline;">
									<button type="submit" class="btn-logout">Logout</button>
								</form>
							</li>
						</ul>
						<span class="nav-user">Welcome, <%= user.getName() %></span>
					</div>
				</nav>
				<main class="container">
					<h1>Profile</h1>

					<% if (error !=null) { %>
						<div class="alert alert-error">
							<%= error %>
						</div>
						<% } %>
							<% if (success !=null) { %>
								<div class="alert alert-success">
									<%= success %>
								</div>
								<% } %>

									<div class="profile-section">
										<h2>Update Profile</h2>
										<form action="${pageContext.request.contextPath}/profile" method="post"
											class="profile-form">
											<input type="hidden" name="action" value="updateProfile">

											<div class="form-group">
												<label for="name">Name:</label>
												<input type="text" id="name" name="name" value="<%= user.getName() %>"
													required>
											</div>
											<div class="form-group">
												<label for="email">Email:</label>
												<input type="email" id="email" name="email"
													value="<%= user.getEmail() %>" required>
											</div>
											<button type="submit" class="btn btn-primary">Update Profile</button>
										</form>
									</div>

									<div class="profile-section">
										<h2>Change Password</h2>
										<form action="${pageContext.request.contextPath}/profile" method="post"
											class="profile-form">
											<input type="hidden" name="action" value="changePassword">

											<div class="form-group">
												<label for="currentPassword">Current Password:</label>
												<input type="password" id="currentPassword" name="currentPassword"
													required>
											</div>
											<div class="form-group">
												<label for="newPassword">New Password:</label>
												<input type="password" id="newPassword" name="newPassword" required>
											</div>
											<div class="form-group">
												<label for="confirmPassword">Confirm New Password:</label>
												<input type="password" id="confirmPassword" name="confirmPassword"
													required>
											</div>
											<button type="submit" class="btn btn-primary">Change Password</button>
										</form>
									</div>
				</main>
			</body>

			</html>