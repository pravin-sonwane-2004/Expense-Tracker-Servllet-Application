<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<%@ page import="java.util.List, com.expensetracker.dto.ExpenseDTO" %>
		<% List<ExpenseDTO> expenses = (List<ExpenseDTO>) request.getAttribute("expenses");
				ExpenseDTO editExpense = (ExpenseDTO) request.getAttribute("editExpense");
				String searchKeyword = (String) request.getAttribute("searchKeyword");
				String error = (String) request.getAttribute("error");
				String success = (String) request.getAttribute("success");
				double reportTotal = (double) request.getAttribute("reportTotal");
				double reportHighest = (double) request.getAttribute("reportHighest");
				double reportLowest = (double) request.getAttribute("reportLowest");
				String activeTab = (String) request.getAttribute("activeTab");
				String reportType = (String) request.getAttribute("reportType");
				String categoryFilter = (String) request.getAttribute("categoryFilter");
				String monthStr = (String) request.getAttribute("monthStr");
				String yearStr = (String) request.getAttribute("yearStr");
				%>
				<!DOCTYPE html>
				<html lang="en">

				<head>
					<meta charset="UTF-8">
					<meta name="viewport" content="width=device-width, initial-scale=1.0">
					<title>Expenses - Expense Tracker</title>
					<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
				</head>

				<body>
					<nav class="navbar">
						<div class="nav-container">
							<a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">Expense Tracker</a>
							<ul class="nav-menu">
								<li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
								<li><a href="${pageContext.request.contextPath}/expenses" class="active">Expenses</a>
								</li>
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
						<h1>Expenses</h1>

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

										<!-- Tab Navigation -->
										<div class="tabs">
											<a href="?tab=list" class="tab <%= " list".equals(activeTab) ? "active" : ""
												%>" onclick="this.href='#list'">List Expenses</a>
											<a href="?tab=add" class="tab <%= editExpense != null ? " active" : "" %>"
												onclick="this.href='#add'">Add Expense</a>
											<a href="?tab=report" class="tab <%= " report".equals(activeTab) ? "active"
												: "" %>" onclick="this.href='#report'">Reports</a>
										</div>

										<!-- List & Search Section -->
										<div id="list" class="tab-content <%= !" report".equals(activeTab) ? "active"
											: "" %>">
											<div class="search-bar">
												<form action="${pageContext.request.contextPath}/expenses" method="get"
													class="search-form">
													<input type="text" name="keyword" placeholder="Search expenses..."
														value="<%= searchKeyword != null ? searchKeyword : "" %>">
													<input type="hidden" name="action" value="search">
													<button type="submit" class="btn btn-primary">Search</button>
													<% if (searchKeyword !=null) { %>
														<a href="${pageContext.request.contextPath}/expenses"
															class="btn btn-secondary">Clear</a>
														<% } %>
												</form>
											</div>

											<table class="table">
												<thead>
													<tr>
														<th>Title</th>
														<th>Amount</th>
														<th>Category</th>
														<th>Date</th>
														<th>Description</th>
														<th>Actions</th>
													</tr>
												</thead>
												<tbody>
													<% if (expenses !=null && !expenses.isEmpty()) { %>
														<% for (ExpenseDTO expense : expenses) { %>
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
																<td>
																	<%= expense.getDescription() !=null &&
																		!expense.getDescription().isEmpty() ?
																		expense.getDescription() : "-" %>
																</td>
																<td class="action-cell">
																	<a href="${pageContext.request.contextPath}/expenses?edit=<%= expense.getId() %>"
																		class="btn btn-sm btn-edit">Edit</a>
																	<form
																		action="${pageContext.request.contextPath}/expenses"
																		method="post" style="display:inline;">
																		<input type="hidden" name="action"
																			value="delete">
																		<input type="hidden" name="id"
																			value="<%= expense.getId() %>">
																		<button type="submit"
																			class="btn btn-sm btn-delete"
																			onclick="return confirm('Delete this expense?')">Delete</button>
																	</form>
																</td>
															</tr>
															<% } %>
																<% } else { %>
																	<tr>
																		<td colspan="6" class="empty-state">No expenses
																			found. Add your first expense!</td>
																	</tr>
																	<% } %>
												</tbody>
											</table>
										</div>

										<!-- Add/Edit Expense Form -->
										<div id="add" class="tab-content <%= editExpense != null ? " active" : "" %>">
											<h2>
												<%= editExpense !=null ? "Edit Expense" : "Add New Expense" %>
											</h2>
											<form action="${pageContext.request.contextPath}/expenses" method="post"
												class="expense-form">
												<% if (editExpense !=null) { %>
													<input type="hidden" name="action" value="edit">
													<input type="hidden" name="id" value="<%= editExpense.getId() %>">
													<% } else { %>
														<input type="hidden" name="action" value="add">
														<% } %>

															<div class="form-group">
																<label for="title">Title:</label>
																<input type="text" id="title" name="title"
																	value="<%= editExpense != null ? editExpense.getTitle() : "" %>"
																	required>
															</div>
															<div class="form-group">
																<label for="amount">Amount:</label>
																<input type="number" id="amount" name="amount"
																	step="0.01" min="0.01"
																	value="<%= editExpense != null ? editExpense.getAmount() : "" %>"
																	required>
															</div>
															<div class="form-group">
																<label for="category">Category:</label>
																<select id="category" name="category" required>
																	<option value="">Select category</option>
																	<% String[] categories={"Food", "Transport"
																		, "Shopping" , "Bills" , "Entertainment"
																		, "Health" , "Education" , "Other" }; String
																		selectedCat=editExpense !=null ?
																		editExpense.getCategory() : "" ; for (String cat
																		: categories) { %>
																		<option value="<%= cat %>"
																			<%=cat.equals(selectedCat) ? "selected" : ""
																			%>><%= cat %>
																		</option>
																		<% } %>
																</select>
															</div>
															<div class="form-group">
																<label for="expenseDate">Date:</label>
																<input type="date" id="expenseDate" name="expenseDate"
																	value="<%= editExpense != null ? editExpense.getExpenseDate() : "" %>"
																	required>
															</div>
															<div class="form-group">
																<label for="description">Description:</label>
																<textarea id="description" name="description"
																	rows="3"><%= editExpense != null ? editExpense.getDescription() : "" %></textarea>
															</div>
															<div class="form-actions">
																<button type="submit" class="btn btn-primary">
																	<%= editExpense !=null ? "Update Expense"
																		: "Add Expense" %>
																</button>
																<% if (editExpense !=null) { %>
																	<a href="${pageContext.request.contextPath}/expenses"
																		class="btn btn-secondary">Cancel</a>
																	<% } %>
															</div>
											</form>
										</div>

										<!-- Reports Section -->
										<div id="report" class="tab-content <%= " report".equals(activeTab) ? "active"
											: "" %>">
											<h2>Reports</h2>
											<form action="${pageContext.request.contextPath}/expenses" method="get"
												class="report-form">
												<input type="hidden" name="action" value="report">

												<div class="form-row">
													<div class="form-group">
														<label for="reportType">Report Type:</label>
														<select id="reportType" name="reportType">
															<option value="all" <%="all" .equals(reportType) ||
																reportType==null ? "selected" : "" %>>All Expenses
															</option>
															<option value="monthly" <%="monthly" .equals(reportType)
																? "selected" : "" %>>Monthly</option>
															<option value="category" <%="category" .equals(reportType)
																? "selected" : "" %>>By Category</option>
														</select>
													</div>
													<div class="form-group">
														<label for="category">Category:</label>
														<select id="category" name="category">
															<option value="">All Categories</option>
															<% for (String cat : new String[]{"Food", "Transport"
																, "Shopping" , "Bills" , "Entertainment" , "Health"
																, "Education" , "Other" }) { %>
																<option value="<%= cat %>" <%=cat.equals(categoryFilter)
																	? "selected" : "" %>><%= cat %>
																</option>
																<% } %>
														</select>
													</div>
													<div class="form-group">
														<label for="month">Month:</label>
														<select id="month" name="month">
															<% for (int m=1; m <=12; m++) { %>
																<option value="<%= m %>"
																	<%=String.valueOf(m).equals(monthStr) ||
																	(monthStr==null &&
																	m==java.time.LocalDate.now().getMonthValue())
																	? "selected" : "" %>>
																	<%= java.time.Month.of(m).toString().charAt(0) +
																		java.time.Month.of(m).toString().substring(1).toLowerCase()
																		%>
																</option>
																<% } %>
														</select>
													</div>
													<div class="form-group">
														<label for="year">Year:</label>
														<input type="number" id="year" name="year"
															value="<%= yearStr != null ? yearStr : String.valueOf(java.time.LocalDate.now().getYear()) %>">
													</div>
												</div>
												<button type="submit" class="btn btn-primary">Generate Report</button>
											</form>

											<% if ("report".equals(activeTab) && expenses !=null && !expenses.isEmpty())
												{ %>
												<div class="report-summary">
													<div class="stat-card">
														<h3>Total</h3>
														<p class="stat-value">$<%= String.format("%.2f", reportTotal) %>
														</p>
													</div>
													<div class="stat-card">
														<h3>Highest</h3>
														<p class="stat-value">$<%= String.format("%.2f", reportHighest)
																%>
														</p>
													</div>
													<div class="stat-card">
														<h3>Lowest</h3>
														<p class="stat-value">$<%= String.format("%.2f", reportLowest)
																%>
														</p>
													</div>
												</div>
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
														<% for (ExpenseDTO expense : expenses) { %>
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
												<% } else if ("report".equals(activeTab)) { %>
													<p class="empty-state">No expenses found for the selected criteria.
													</p>
													<% } %>
										</div>
					</main>
				</body>

				</html>