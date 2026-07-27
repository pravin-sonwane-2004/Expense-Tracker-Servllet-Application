<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
		<!DOCTYPE html>
		<html lang="en">

		<head>
			<meta charset="UTF-8">
			<meta name="viewport" content="width=device-width, initial-scale=1.0">
			<title>${param.title} - Expense Tracker</title>
			<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
		</head>

		<body>
			<c:if test="${not empty sessionScope.user}">
				<nav class="navbar">
					<div class="nav-container">
						<a href="${pageContext.request.contextPath}/dashboard" class="nav-brand">Expense Tracker</a>
						<ul class="nav-menu">
							<li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
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
			</c:if>
			<main class="container">