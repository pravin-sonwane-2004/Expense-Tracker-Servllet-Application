<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<%@ page import="java.util.List, com.expensetracker.dto.ExpenseDTO" %>
		<% double totalExpense=(double) request.getAttribute("totalExpense"); List<ExpenseDTO> recentExpenses = (List
			<ExpenseDTO>) request.getAttribute("recentExpenses");
				double highestExpense = (double) request.getAttribute("highestExpense");
				double lowestExpense = (double) request.getAttribute("lowestExpense");
				%>
				<!DOCTYPE html>
				<html lang="en">

				<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Dashboard - Expense Tracker</title>
					<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
				</head>

				<body>
					<nav class="navbar">
						<div class="nav-container">
							<a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">Expense Tracker</a>
							<ul class="nav-menu">
								<li><a href="${pageContext.request.contextPath}/dashboard" class="active">Dashboard</a>
								</li>
								<li><a href="${pageContext.request.contextPath}/expenses">Expenses</a></li>
								<li><a href="${pageContext.request.contextPath}/profile">Profile</a></li>
								<li>
									<form action="${pageContext.request.contextPath}/api/logout" method="post"
										style="display:inline;">
										<button type="submit" class="btn-logout">Logout</button>
									</form>
								</li>
							</ul>
							<span class="nav-user">Welcome, ${sessionScope.user.name}</span>
						</div>
					</nav>
					<main class="container">
						<h1>Dashboard</h1>

						<div class="stats-grid">
							<div class="stat-card stat-total">
								<h3>Total Expenses</h3>
								<p class="stat-value">$<%= String.format("%.2f", totalExpense) %>
								</p>
							</div>
							<div class="stat-card stat-high">
								<h3>Highest Expense</h3>
								<p class="stat-value">$<%= String.format("%.2f", highestExpense) %>
								</p>
							</div>
							<div class="stat-card stat-low">
								<h3>Lowest Expense</h3>
								<p class="stat-value">$<%= String.format("%.2f", lowestExpense) %>
								</p>
							</div>
							<div class="stat-card stat-count">
								<h3>Total Entries</h3>
								<p class="stat-value">
									<%= recentExpenses.size() %>
								</p>
							</div>
						</div>

						<div class="section">
							<h2>Recent Expenses</h2>
							<% if (recentExpenses.isEmpty()) { %>
								<p class="empty-state">No expenses yet. <a
										href="${pageContext.request.contextPath}/expenses">Add your first expense</a>
								</p>
								<% } else { %>
									<table class="table">
										<thead>
											<tr>
												<th>Title</th>
												<th>Amount</th>
												<th>Category</th>
												<th>Date</th>
											</tr>
										</thead>
										<tbody>
											<% for (ExpenseDTO expense : recentExpenses) { %>
												<tr>
													<td>
														<%= expense.getTitle() %>
													</td>
													<td>$<%= String.format("%.2f", expense.getAmount()) %>
													</td>
													<td><span class="category-badge">
															<%= expense.getCategory() %>
														</span></td>
													<td>
														<%= expense.getExpenseDate() %>
													</td>
												</tr>
												<% } %>
										</tbody>
									</table>
									<% } %>
						</div>
					</main>
				</body>

				</html>