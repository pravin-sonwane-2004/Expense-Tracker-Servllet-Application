# Expense Tracker

A full-stack Java web application for tracking personal expenses built with Jakarta Servlets, JDBC, MySQL, JSP, and minimal JavaScript. The application follows a strict **Entity → DTO → Repository → Service → Controller** layered architecture with both **JSP server-side pages** and **JSON REST APIs**.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Layered Architecture Deep Dive](#2-layered-architecture-deep-dive)
3. [Internal Working](#3-internal-working)
4. [Session Working](#4-session-working)
5. [Error Working](#5-error-working)
6. [API Working](#6-api-working)
7. [Page Routes vs API Routes](#7-page-routes-vs-api-routes)
8. [Request-Response Lifecycle](#8-request-response-lifecycle)
9. [Database Schema](#9-database-schema)
10. [Setup & Deployment](#10-setup--deployment)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           BROWSER                                       │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  │
│  │ login.jsp   │  │ register.jsp │  │ dashboard.jsp│  │expenses.jsp│  │
│  │ (server-    │  │ (server-     │  │ (server-     │  │ (server-   │  │
│  │  rendered)  │  │  rendered)   │  │  rendered)   │  │  rendered) │  │
│  └──────┬──────┘  └──────┬───────┘  └──────┬───────┘  └──────┬─────┘  │
└─────────┼────────────────┼─────────────────┼──────────────────┼────────┘
          │                │                 │                  │
          │  POST form     │  POST form      │  GET page        │  GET/POST
          ▼                ▼                 ▼                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         APACHE TOMCAT 11                                │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    AUTH FILTER                                    │  │
│  │  @WebFilter("/dashboard", "/expenses", "/profile",               │  │
│  │              "/api/dashboard", "/api/expenses", "/api/profile")   │  │
│  │  - Checks HttpSession for "user" attribute                       │  │
│  │  - Page routes → redirect to /login on failure                   │  │
│  │  - API routes  → return JSON 401 on failure                      │  │
│  └──────────────────────────┬───────────────────────────────────────┘  │
│                             │                                          │
│              ┌──────────────┼──────────────────┐                       │
│              ▼              ▼                  ▼                       │
│  ┌──────────────────┐  ┌────────────┐  ┌─────────────────────┐        │
│  │  PAGE SERVLETS   │  │ API SERVLETS│  │   FILTERS/LISTENERS │        │
│  │ (JSP rendering)  │  │ (JSON)     │  │                     │        │
│  ├──────────────────┤  ├────────────┤  │ AuthFilter          │        │
│  │ LoginPageServlet │  │ LoginServlet│  │ AppListener         │        │
│  │ RegisterPageSrv  │  │ RegisterSrv│  └─────────────────────┘        │
│  │ DashboardPageSrv │  │ExpenseSrv  │                                 │
│  │ ExpensesPageSrv  │  │ProfileSrv  │                                 │
│  │ ProfilePageSrv   │  │LogoutSrv   │                                 │
│  └────────┬─────────┘  └─────┬──────┘                                 │
│           │                  │                                         │
│           ▼                  ▼                                         │
│  ┌────────────────────────────────────────────────────────┐           │
│  │                  SERVICE LAYER                         │           │
│  │  UserService          ExpenseService                   │           │
│  │  - validateLogin()     - validateExpense()             │           │
│  │  - validateRegister()  - getTotalExpense()             │           │
│  │  - changePassword()    - getReportExpenses()           │           │
│  │  - getUserDTOById()    - getExpenseDTOsByUserId()      │           │
│  │  - loginUser()         - convertToDTO()                │           │
│  └────────────────────┬───────────────────────────────────┘           │
│                       │                                               │
│                       ▼                                               │
│  ┌────────────────────────────────────────────────────────┐           │
│  │                REPOSITORY LAYER                        │           │
│  │  Interfaces:  UserRepository  ExpenseRepository        │           │
│  │  Impls:       UserRepositoryImpl  ExpenseRepositoryImpl│           │
│  │  - CRUD via JDBC PreparedStatements                    │           │
│  │  - SQL queries mapped to Model objects                 │           │
│  └────────────────────┬───────────────────────────────────┘           │
│                       │                                               │
│                       ▼                                               │
│  ┌────────────────────────────────────────────────────────┐           │
│  │                     UTILITY LAYER                      │           │
│  │  DBConnection  - MySQL connection pool                 │           │
│  │  GsonProvider  - JSON serializer w/ java.time support  │           │
│  │  DBException   - Runtime exception wrapper             │           │
│  └────────────────────┬───────────────────────────────────┘           │
│                       │                                               │
└───────────────────────┼───────────────────────────────────────────────┘
                        │
                        ▼
              ┌─────────────────┐
              │     MYSQL       │
              │  expense_tracker│
              │  ├─ users       │
              │  └─ expenses    │
              └─────────────────┘
```

### Three-Layer Data Flow (per request)

```
REQUEST → [Controller] → [Service] → [Repository] → [Database]
                              ↓
                         [DTO ↔ Entity]
                              ↓
RESPONSE ← [Controller] ← [Service] ← [Repository] ← [Database]
```

---

## 2. Layered Architecture Deep Dive

### Layer 1: Entity Layer (`com.expensetracker.model`)

Pure Java Beans that map 1:1 to database tables. These carry data between Repository and Service layers.

```java
// Expense.java - Maps to `expenses` table
public class Expense {
    private int id;              // PRIMARY KEY AUTO_INCREMENT
    private String title;        // NOT NULL
    private double amount;       // DECIMAL(10,2) NOT NULL
    private String category;     // VARCHAR(50) NOT NULL
    private LocalDate expenseDate; // DATE NOT NULL
    private String description;  // TEXT
    private int userId;          // FOREIGN KEY → users(id) CASCADE DELETE
}

// User.java - Maps to `users` table
public class User {
    private int id;              // PRIMARY KEY AUTO_INCREMENT
    private String name;         // NOT NULL
    private String email;        // UNIQUE NOT NULL
    private String password;     // NOT NULL (plaintext for this version)
    private LocalDateTime createdAt; // TIMESTAMP DEFAULT CURRENT_TIMESTAMP
}
```

### Layer 2: DTO Layer (`com.expensetracker.dto`)

Data Transfer Objects that carry data to the view layer. They exclude sensitive fields (like password) and provide a clean contract for the presentation layer.

```java
// ExpenseDTO.java - No sensitive data, used for JSP/API responses
public class ExpenseDTO {
    private int id;
    private String title;
    private double amount;
    private String category;
    private LocalDate expenseDate;
    private String description;
    // password deliberately excluded
}

// UserDTO.java - No password field
public class UserDTO {
    private int id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    // password deliberately excluded
}
```

### Layer 3: Repository Layer (`com.expensetracker.repository`)

Provides an **interface-contract** with concrete implementations. This is the data access layer that directly interacts with the database via JDBC.

**Interfaces (contracts):**
```java
public interface ExpenseRepository {
    void save(Expense expense);
    void update(Expense expense);
    void delete(int expenseId, int userId);
    Expense findById(int expenseId, int userId);
    List<Expense> findByUserId(int userId);
    List<Expense> search(int userId, String keyword);
    double getTotalExpense(int userId);
    List<Expense> findRecent(int userId, int limit);
    double getHighestExpense(int userId);
    double getLowestExpense(int userId);
    List<Expense> findByMonth(int userId, int year, int month);
    List<Expense> findByCategory(int userId, String category);
}

public interface UserRepository {
    void save(User user);
    User findByEmailAndPassword(String email, String password);
    User findById(int userId);
    boolean isEmailExists(String email);
    void update(User user);
    boolean changePassword(int userId, String oldPassword, String newPassword);
}
```

**Implementation pattern:**
```java
public class ExpenseRepositoryImpl implements ExpenseRepository {
    @Override
    public void save(Expense expense) {
        // 1. Get Connection from DBConnection
        // 2. Prepare SQL INSERT with RETURN_GENERATED_KEYS
        // 3. Set parameters from expense object
        // 4. ExecuteUpdate
        // 5. Read generated ID back into expense object
        // 6. Auto-close resources via try-with-resources
        // 7. Wrap SQLException in DBException (unchecked)
    }

    private Expense mapExpense(ResultSet rs) throws SQLException {
        // Maps ResultSet columns → Expense fields
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setTitle(rs.getString("title"));
        // ... etc
        return expense;
    }
}
```

**Why Repository over DAO?**
- DAOs are concrete classes with hard dependencies
- Repositories are interface-based, allowing:
  - Easy mocking in unit tests
  - Swapping implementations (e.g., JPA vs JDBC)
  - Dependency injection via constructor

### Layer 4: Service Layer (`com.expensetracker.service`)

Business logic layer that orchestrates operations between controllers and repositories.

```java
public class ExpenseService {
    private ExpenseRepository expenseRepository;  // PROGRAM TO INTERFACE

    // Constructor injection for testability
    public ExpenseService() {
        this.expenseRepository = new ExpenseRepositoryImpl();  // default
    }
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;  // injected for testing
    }

    // Business operation + DTO conversion
    public List<ExpenseDTO> getExpenseDTOsByUserId(int userId) {
        return expenseRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)   // Entity → DTO
                .collect(Collectors.toList());
    }

    // Validation logic
    public String validateExpense(String title, String amountStr, 
                                   String category, String dateStr) {
        if (title == null || title.trim().isEmpty()) return "Title is required";
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) return "Amount must be greater than zero";
        } catch (NumberFormatException e) { return "Invalid amount"; }
        // ... more validation
        return null;  // null = no error
    }

    // Entity → DTO conversion
    private ExpenseDTO convertToDTO(Expense expense) {
        return new ExpenseDTO(
            expense.getId(), expense.getTitle(), expense.getAmount(),
            expense.getCategory(), expense.getExpenseDate(),
            expense.getDescription(), expense.getUserId()
        );
    }
}
```

**Service responsibilities:**
1. **Validation** - All input validation happens here, never in controllers
2. **Business Logic** - Calculations (totals, reports), business rules
3. **DTO Conversion** - Entity ↔ DTO mapping
4. **Orchestration** - Multiple repository calls in one transaction
5. **Error handling** - Business exceptions, validation errors

### Layer 5: Controller Layer (`com.expensetracker.controller`)

Two types of controllers:

**A. Page Servlets** - Render JSP views with server-side data
```java
@WebServlet("/dashboard")  // ← PAGE ROUTE (no /api/ prefix)
public class DashboardPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // 1. Check authentication (via AuthFilter)
        // 2. Call Service layer to get data
        double totalExpense = expenseService.getTotalExpense(userId);
        List<ExpenseDTO> recentExpenses = expenseService.getRecentExpenseDTOs(userId, 5);

        // 3. Set request attributes for JSP
        request.setAttribute("totalExpense", totalExpense);
        request.setAttribute("recentExpenses", recentExpenses);

        // 4. Forward to JSP for rendering
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp")
               .forward(request, response);
    }
}
```

**B. API Servlets** - Return JSON responses
```java
@WebServlet("/api/login")  // ← API ROUTE (has /api/ prefix)
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        // 1. Parse parameters from request body/form
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // 2. Validate via Service layer
        String error = userService.validateLogin(email, password);
        if (error != null) {
            // 3. Return JSON error response
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", error);
            response.getWriter().write(gson.toJson(result));
            return;
        }

        // 4. Authenticate via Service layer
        User user = userService.loginUser(email, password);
        if (user == null) {
            // Return JSON error
            result.put("error", "Invalid credentials");
            response.getWriter().write(gson.toJson(result));
            return;
        }

        // 5. Create session
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        // 6. Handle Remember Me cookie
        // 7. Redirect or return JSON
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
```

---

## 3. Internal Working

### Complete Request Flow for Adding an Expense (JSP Form)

```
User fills form on expenses.jsp
         │
         │ POST /expenses (form submit)
         ▼
┌────────────────────────────────────────────────────────────┐
│ AUTH FILTER                                                │
│ 1. Get HttpSession via request.getSession(false)           │
│ 2. Check session.getAttribute("user") != null              │
│ 3. If null: redirect to /login                            │
│ 4. If valid: chain.doFilter() → forwards to servlet       │
└────────────────────────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────┐
│ EXPENSES PAGE SERVLET (ExpensesPageServlet.java)           │
│                                                           │
│ doPost():                                                  │
│ 1. Get session, get User from session                     │
│ 2. Read form parameters: title, amount, category, etc.    │
│ 3. Call expenseService.validateExpense(title, amount, ...) │
│    └── If error returned → set "error" request attribute  │
│        → call doGet() → show form with error message      │
│ 4. Create new Expense entity object                       │
│ 5. Call expenseService.addExpense(expense)                │
│ 6. Set "success" request attribute                        │
│ 7. Call doGet() → reload page with success message        │
└──────────────────────┬────────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────────┐
│ EXPENSE SERVICE                                            │
│                                                            │
│ addExpense(expense):                                       │
│ 1. Delegate to expenseRepository.save(expense)             │
│ 2. No return value (void)                                  │
│                                                            │
│ The Repository sets the generated ID on the expense object │
└──────────────────────┬────────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────────┐
│ EXPENSE REPOSITORY IMPL                                    │
│                                                            │
│ save(expense):                                             │
│ 1. Get Connection from DBConnection.getConnection()        │
│ 2. Prepare INSERT INTO expenses (...) VALUES (?, ..., ?)   │
│    with Statement.RETURN_GENERATED_KEYS                    │
│ 3. Set parameters from expense fields                      │
│ 4. Execute update                                          │
│ 5. Get generated keys (auto-increment ID)                  │
│ 6. Set expense.setId(generatedId)                          │
│ 7. try-with-resources auto-closes Statement, Connection    │
│ 8. On SQLException → wrap in DBException (unchecked)      │
└──────────────────────┬────────────────────────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │     MYSQL       │
              │  INSERT INTO    │
              │  expenses ...   │
              └─────────────────┘

After doPost() completes, it calls doGet() which:
┌────────────────────────────────────────────────────────────┐
│ doGet():                                                   │
│ 1. Call expenseService.getExpenseDTOsByUserId(userId)     │
│    → Repository.findByUserId(userId)                      │
│    → Service.convertToDTO() for each Expense              │
│    → Returns List<ExpenseDTO>                             │
│ 2. Set request attributes                                 │
│ 3. Forward to /WEB-INF/views/expenses.jsp                 │
└──────────────────────┬────────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────────────────────┐
│ EXPENSES.JSP                                              │
│ 1. Java scriptlets extract request attributes             │
│ 2. Check for "error" or "success" → show alert divs      │
│ 3. Iterate over expenses list → render HTML table rows   │
│ 4. Server sends complete HTML to browser                  │
└────────────────────────────────────────────────────────────┘
```

### Complete Request Flow for Login (JSP Form → API)

```
login.jsp (server-rendered form)
         │
         │ User clicks Login button
         │ POST /api/login (form action)
         ▼
┌────────────────────────────────────────────────────────────┐
│ LOGIN SERVLET (LoginServlet.java)                          │
│                                                           │
│ doPost():                                                  │
│ 1. Read email, password, rememberMe from request params   │
│ 2. Call userService.validateLogin(email, password)        │
│    └── If error: return JSON {success:false, error:"..."} │
│ 3. Call userService.loginUser(email.trim().toLowerCase(), │
│                              password)                    │
│    └── UserRepository.findByEmailAndPassword(email, pwd)  │
│    └── If null: return JSON {success:false,               │
│                              error:"Invalid credentials"} │
│ 4. Create HttpSession: request.getSession()               │
│ 5. Store user in session: session.setAttribute("user", u) │
│ 6. Set session timeout: 30 minutes                        │
│ 7. Handle Remember Me cookie (7-day cookie)               │
│ 8. Redirect: response.sendRedirect("/Expense-Tracker/     │
│                                     dashboard")           │
└────────────────────────────────────────────────────────────┘
         │
         │ Browser follows 302 redirect to /dashboard
         ▼
┌────────────────────────────────────────────────────────────┐
│ AUTH FILTER                                                │
│ 1. request.getSession(false) → returns existing session   │
│ 2. session.getAttribute("user") → returns User object     │
│ 3. Valid → chain.doFilter() → DashboardPageServlet       │
└────────────────────────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────┐
│ DASHBOARD PAGE SERVLET                                     │
│ → Calls services → sets attributes → forwards to JSP      │
└────────────────────────────────────────────────────────────┘
```

---

## 4. Session Working

### Session Lifecycle

```
                    REQUEST
                       │
                       ▼
              ┌─────────────────┐
              │ CONTAINER CHECKS │
              │ FOR SESSION      │
              └────────┬────────┘
                       │
              ┌────────┴────────┐
              │                 │
              ▼                 ▼
       ┌────────────┐   ┌──────────────┐
       │ NO SESSION │   │ SESSION      │
       │            │   │ EXISTS       │
       └──────┬─────┘   └──────┬───────┘
              │                │
              ▼                ▼
       ┌────────────┐   ┌──────────────┐
       │ Create new │   │ Check user   │
       │ session    │   │ attribute    │
       └────────────┘   └──────┬───────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
             ┌────────────┐      ┌────────────────┐
             │ user found │      │ user NOT found │
             └──────┬─────┘      └───────┬────────┘
                    │                     │
                    ▼                     ▼
             ┌────────────┐      ┌────────────────┐
             │ Proceed to │      │ Page: redirect │
             │ servlet    │      │ to /login      │
             └────────────┘      │ API: 401 JSON  │
                                 └────────────────┘
```

### Session Creation (LoginServlet.java)

```java
// This creates a NEW session if one doesn't exist, or returns existing
HttpSession session = request.getSession();  // no 'false' = create if absent

// Store authenticated user object
session.setAttribute("user", user);  // User object with id, name, email

// Set inactivity timeout (overrides web.xml setting)
session.setMaxInactiveInterval(30 * 60);  // 30 minutes in seconds

// Tomcat generates a unique JSESSIONID cookie automatically
// Set-Cookie: JSESSIONID=ABC123...; Path=/Expense-Tracker/; HttpOnly
```

### Session Verification (AuthFilter.java)

```java
public void doFilter(ServletRequest request, ServletResponse response, 
                     FilterChain chain) {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // false = do NOT create a new session, return null if none exists
    HttpSession session = httpRequest.getSession(false);

    if (session == null || session.getAttribute("user") == null) {
        String path = httpRequest.getRequestURI()
                      .substring(httpRequest.getContextPath().length());

        if (path.startsWith("/dashboard") || /* page routes */) {
            // Page request: redirect to login page
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        } else {
            // API request: return JSON 401
            httpResponse.setStatus(401);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter()
                .write("{\"success\":false,\"error\":\"Not authenticated\"}");
        }
        return;  // BLOCK the request
    }

    // Session valid, user authenticated → continue to servlet
    chain.doFilter(request, response);
}
```

### Session Invalidation (LogoutServlet.java)

```java
protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();  // Destroys session, removes all attributes
    }
    // Also clear Remember Me cookie
    Cookie cookie = new Cookie("rememberedEmail", "");
    cookie.setMaxAge(0);  // 0 = delete immediately
    cookie.setPath(request.getContextPath() + "/");
    response.addCookie(cookie);

    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    response.getWriter().write(gson.toJson(result));
}
```

### Session Configuration (web.xml)

```xml
<session-config>
    <session-timeout>30</session-timeout>  <!-- Minutes of inactivity -->
</session-config>
```

### Session Attributes Table

| Attribute | Type | Set By | Used By | Description |
|-----------|------|--------|---------|-------------|
| `user` | `User` | LoginServlet | All authenticated pages/APIs | Logged-in user object (id, name, email) |

### Remember Me Cookie Mechanism

```java
// LoginServlet.java - On successful login with "rememberMe" checkbox checked
if ("on".equals(rememberMe)) {
    Cookie cookie = new Cookie("rememberedEmail", email);  // Stores email
    cookie.setMaxAge(7 * 24 * 60 * 60);  // 7 days validity
    cookie.setHttpOnly(true);            // Not accessible via JavaScript
    cookie.setPath(request.getContextPath() + "/");
    response.addCookie(cookie);
}

// LoginPageServlet.java - Reads cookie to pre-fill login form
Cookie[] cookies = request.getCookies();
if (cookies != null) {
    for (Cookie cookie : cookies) {
        if ("rememberedEmail".equals(cookie.getName())) {
            request.setAttribute("rememberedEmail", cookie.getValue());
            // JSP reads: <input value='<%= rememberedEmail %>'>
            break;
        }
    }
}
```

---

## 5. Error Working

### Error Handling Architecture

```
                    APPLICATION ERROR OCCURS
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
   ┌────────────┐   ┌──────────────┐   ┌────────────┐
   │ Validation │   │ Database     │   │ Runtime    │
   │ Error      │   │ Exception    │   │ Exception  │
   └──────┬─────┘   └──────┬───────┘   └──────┬─────┘
          │                │                   │
          ▼                ▼                   ▼
   ┌────────────┐   ┌──────────────┐   ┌──────────────┐
   │ Service    │   │ DBException  │   │ 500 Error    │
   │ returns    │   │ (unchecked)  │   │ Page or      │
   │ String     │   │ wraps        │   │ Stack Trace  │
   │ error msg  │   │ SQLException │   │              │
   └──────┬─────┘   └──────┬───────┘   └──────┬───────┘
          │                │                   │
          ▼                ▼                   ▼
   ┌──────────────────────────────────────────────┐
   │           ERROR DISPLAY                       │
   │                                               │
   │  Page requests:                               │
   │    - Set request attribute "error"            │
   │    - JSP checks <c:if test="${not empty err}">│
   │    - Shows <div class="alert alert-error">    │
   │                                               │
   │  API requests:                                │
   │    - Return JSON {success:false, error:"..."} │
   │    - Front-end checks result.success           │
   │    - Shows error message in UI                │
   │                                               │
   │  Unhandled errors:                            │
   │    - web.xml error-page config                │
   │    - 404 → error.html                         │
   │    - 500 → error.html                         │
   │    - Any Throwable → error.html               │
   └──────────────────────────────────────────────┘
```

### Error Types and Handling

#### 1. Validation Errors (Service Layer)

```java
// Service returns String: null = success, non-null = error message
String validationError = expenseService.validateExpense(title, amount, category, date);
if (validationError != null) {
    // Page Servlet: set request attribute, re-render JSP
    request.setAttribute("error", validationError);
    request.getRequestDispatcher("/WEB-INF/views/expenses.jsp").forward(request, response);
    return;

    // API Servlet: return JSON error
    Map<String, Object> result = new HashMap<>();
    result.put("success", false);
    result.put("error", validationError);
    response.getWriter().write(gson.toJson(result));
    return;
}
```

Possible validation errors:
```
- "Title is required"
- "Amount must be greater than zero"
- "Invalid amount"
- "Category is required"
- "Invalid date format"
- "Name is required"
- "Invalid email format"
- "Password must be at least 6 characters"
- "Passwords do not match"
- "Email already registered"
- "Email and password are required"
- "Current password is required"
- "New password must be at least 6 characters"
```

#### 2. Database Errors (DBException)

```java
// DBException.java - Custom unchecked exception
public class DBException extends RuntimeException {
    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Repository layer wraps SQLException in DBException
try (Connection conn = DBConnection.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    // ... database operation
} catch (SQLException e) {
    throw new DBException("Failed to add expense", e);  // Rethrow as unchecked
}
```

Common DBException scenarios:
```
- "Failed to connect to database. Ensure MySQL is running..."
- "Failed to add expense"
- "Failed to update expense"
- "Failed to delete expense"
- "Failed to get expenses"
- "Failed to search expenses"
- "Failed to register user"
- "Failed to login user"
```

#### 3. HTTP Error Configuration (web.xml)

```xml
<!-- web.xml error page configuration -->
<error-page>
    <error-code>404</error-code>
    <location>/error.html</location>
</error-page>

<error-page>
    <error-code>500</error-code>
    <location>/error.html</location>
</error-page>

<error-page>
    <exception-type>java.lang.Throwable</exception-type>
    <location>/error.html</location>
</error-page>
```

#### 4. Session/Authentication Errors

| Scenario | Page Route | API Route |
|----------|------------|-----------|
| No session | Redirect to `/login` | JSON 401 `{"success":false,"error":"Not authenticated"}` |
| Session expired | Redirect to `/login` | JSON 401 `{"success":false,"error":"Not authenticated"}` |
| Invalid login | CORS/redirect | JSON `{"success":false,"error":"Invalid email or password"}` |

#### 5. Success Messages

```java
// Page Servlets use "success" request attribute
request.setAttribute("success", "Expense added successfully!");
// JSP checks: <% if (success != null) { %>
// Shows: <div class="alert alert-success">

request.setAttribute("success", "Expense updated successfully!");
request.setAttribute("success", "Expense deleted successfully!");
request.setAttribute("success", "Registration successful! Please login.");
request.setAttribute("success", "Profile updated successfully!");
request.setAttribute("success", "Password changed successfully!");
```

### Error Display in JSP

```jsp
<!-- Generic pattern in all JSP pages -->
<% if (error != null) { %>
    <div class="alert alert-error"><%= error %></div>
<% } %>
<% if (success != null) { %>
    <div class="alert alert-success"><%= success %></div>
<% } %>

<!-- CSS for alerts -->
.alert-error {
    background: #f8d7da;     /* Red background */
    color: #721c24;           /* Dark red text */
    border: 1px solid #f5c6cb;
}

.alert-success {
    background: #d4edda;     /* Green background */
    color: #155724;           /* Dark green text */
    border: 1px solid #c3e6cb;
}
```

---

## 6. API Working

### API Endpoint Reference

| Method | URL | Servlet | Auth Req | Description |
|--------|-----|---------|----------|-------------|
| GET | `/login` | LoginPageServlet | No | Show login page |
| GET | `/register` | RegisterPageServlet | No | Show register page |
| POST | `/api/login` | LoginServlet | No | Authenticate user, create session |
| POST | `/api/register` | RegisterServlet | No | Create new user account |
| POST | `/api/logout` | LogoutServlet | No | Invalidate session, clear cookies |
| GET | `/dashboard` | DashboardPageServlet | Yes | Show dashboard with stats JSP |
| GET | `/expenses` | ExpensesPageServlet | Yes | Show expenses page JSP |
| POST | `/expenses` | ExpensesPageServlet | Yes | CRUD operations via form |
| GET | `/profile` | ProfilePageServlet | Yes | Show profile page JSP |
| POST | `/profile` | ProfilePageServlet | Yes | Update profile/change password |
| GET | `/api/dashboard` | DashboardServlet | Yes | Get dashboard JSON data |
| GET | `/api/expenses` | ExpenseServlet | Yes | List/search/report expenses JSON |
| POST | `/api/expenses` | ExpenseServlet | Yes | Add/edit/delete expense JSON |
| GET | `/api/profile` | ProfileServlet | Yes | Get user profile JSON |
| POST | `/api/profile` | ProfileServlet | Yes | Update profile/password JSON |

### Detailed API Specifications

#### POST `/api/login`

**Request** (form-urlencoded):
```
email=user@example.com&password=secret123&rememberMe=on
```

**Success Response** (redirect):
```
302 Location: /Expense-Tracker/dashboard
Set-Cookie: JSESSIONID=ABC123...; Path=/Expense-Tracker/; HttpOnly
Set-Cookie: rememberedEmail=user@example.com; Max-Age=604800; Path=/Expense-Tracker/
```

**Error Response** (JSON):
```json
{"success": false, "error": "Invalid email or password"}
```

#### POST `/api/register`

**Request** (form-urlencoded):
```
name=John&email=john@example.com&password=secret123&confirmPassword=secret123
```

**Success Response** (redirect):
```
302 Location: /Expense-Tracker/login
```

**Error Response** (JSON):
```json
{"success": false, "error": "Email already registered"}
```

#### GET `/api/dashboard` (Auth required)

**Response:**
```json
{
    "success": true,
    "totalExpense": 1250.50,
    "recentExpenses": [
        {
            "id": 5,
            "title": "Groceries",
            "amount": 85.50,
            "category": "Food",
            "expenseDate": "2026-07-25",
            "description": "Weekly groceries",
            "userId": 1
        }
    ],
    "highestExpense": 500.00,
    "lowestExpense": 5.00,
    "user": {
        "id": 1,
        "name": "John",
        "email": "john@example.com",
        "password": "secret",
        "createdAt": "Jul 27, 2026, 10:00:00 AM"
    }
}
```

#### GET `/api/expenses` (Auth required)

**Query Parameters:**
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `action` | String | No | `list` (default), `edit`, `search`, `report` |
| `id` | Integer | For `edit` | Expense ID to fetch |
| `keyword` | String | For `search` | Search term |
| `reportType` | String | For `report` | `all`, `monthly`, `category` |
| `category` | String | For `report` | Category filter |
| `month` | Integer(1-12) | For `monthly` | Month number |
| `year` | Integer | For `monthly` | Year |

**Response (list):**
```json
{
    "success": true,
    "expenses": [
        {"id": 1, "title": "Lunch", "amount": 15.50, "category": "Food", "expenseDate": "2026-07-26", "description": "", "userId": 1}
    ]
}
```

**Response (edit):**
```json
{
    "success": true,
    "expense": {"id": 1, "title": "Lunch", "amount": 15.50, "category": "Food", ...}
}
```

**Response (report):**
```json
{
    "success": true,
    "reportExpenses": [...],
    "reportTotal": 1250.50,
    "reportHighest": 500.00,
    "reportLowest": 5.00
}
```

#### POST `/api/expenses` (Auth required)

**Query Parameter:** `action=add|edit|delete`

**Add request:**
```
title=Groceries&amount=85.50&category=Food&expenseDate=2026-07-25&description=Weekly
```
**Response:** `{"success": true, "message": "Expense added successfully"}`

**Edit request:**
```
id=5&title=Groceries&amount=90.00&category=Food&expenseDate=2026-07-25&description=Updated
```
**Response:** `{"success": true, "message": "Expense updated successfully"}`

**Delete request:**
```
id=5
```
**Response:** `{"success": true, "message": "Expense deleted successfully"}`

#### POST `/api/logout`

**Response:** `{"success": true}`

### Auth Error Response

**401 Unauthorized:**
```json
{"success": false, "error": "Not authenticated"}
```

---

## 7. Page Routes vs API Routes

### Page Routes (JSP Server-Side Rendering)

These are the primary way users interact with the application. Forms submit to page servlets which process data, set request attributes, and forward to JSP for rendering.

```
/login         → LoginPageServlet   → login.jsp     (no auth)
/register      → RegisterPageServlet → register.jsp  (no auth)
/dashboard     → DashboardPageServlet → dashboard.jsp (auth required)
/expenses      → ExpensesPageServlet  → expenses.jsp (auth required)
/profile       → ProfilePageServlet   → profile.jsp  (auth required)
```

### API Routes (JSON Responses)

These provide JSON data for potential frontend integrations. They return structured JSON rather than HTML.

```
/api/login      → LoginServlet     (JSON/redirect)
/api/register   → RegisterServlet  (JSON/redirect)
/api/logout     → LogoutServlet    (JSON)
/api/dashboard  → DashboardServlet (JSON)
/api/expenses   → ExpenseServlet   (JSON)
/api/profile    → ProfileServlet   (JSON)
```

### Key Difference: Error Handling

| | Page Route | API Route |
|---|------------|-----------|
| Auth failure | Redirect to `/login` | JSON 401 `{"success":false,"error":"Not authenticated"}` |
| Validation error | Set request attribute, re-render JSP | JSON `{"success":false,"error":"message"}` |
| Success | Set request attribute, re-render JSP with success message | JSON `{"success":true, ...}` or redirect |

---

## 8. Request-Response Lifecycle

### Complete Request Lifecycle Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. CLIENT REQUEST                                                    │
│    Browser sends HTTP request to Tomcat                              │
│    URL: http://localhost:8080/Expense-Tracker/dashboard              │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ 2. TOMCAT RECEIVES REQUEST                                          │
│    - Determines context path: /Expense-Tracker                      │
│    - Wraps in HttpServletRequest/HttpServletResponse objects        │
│    - Checks web.xml for configuration                               │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ 3. FILTER CHAIN                                                     │
│    - AuthFilter matches /dashboard URL pattern                      │
│    - Calls doFilter(request, response, chain)                       │
│    - Checks: session.getAttribute("user")                           │
│    - If invalid: redirect to /login (page) OR return JSON 401 (API) │
│    - If valid: chain.doFilter() → proceed                           │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ 4. SERVLET (Controller)                                              │
│    - Tomcat calls servlet.service(request, response)                │
│    - service() dispatches to doGet() or doPost()                    │
│    - Reads request parameters, session attributes                   │
│    - Calls Service layer methods                                    │
│    - Sets request attributes for JSP                                │
│    - Forwards to JSP via: request.getRequestDispatcher("/WEB-INF/   │
│                           views/dashboard.jsp").forward(req, res)   │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ 5. SERVICE LAYER                                                     │
│    - Validates inputs                                               │
│    - Calls Repository methods                                       │
│    - Converts entities to DTOs                                      │
│    - Returns data to servlet                                        │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ 6. REPOSITORY LAYER                                                  │
│    - Gets DB connection from DBConnection.getConnection()           │
│    - Creates PreparedStatement for SQL query                        │
│    - Sets parameters, executes query                                │
│    - Maps ResultSet to Model objects                                │
│    - Closes resources (try-with-resources)                          │
│    - Returns data to Service layer                                  │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ 7. JSP VIEW RENDERING                                                │
│    - JSP engine (Jasper) compiles JSP to servlet if needed          │
│    - Executes Java scriptlets                                       │
│    - Reads request attributes                                       │
│    - Generates HTML output                                          │
│    - Writes to HttpServletResponse writer                           │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ 8. HTTP RESPONSE                                                     │
│    - Tomcat sends generated HTML back to browser                    │
│    - Includes Set-Cookie headers if session created                 │
│    - Browser renders HTML page                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### JSP Processing Chain

```
                    JSP FILE
                       │
                       ▼
              ┌─────────────────┐
              │ TRANSLATION     │
              │ (First request  │
              │  only)          │
              │ JSP → .java     │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐
              │ COMPILATION     │
              │ (First request  │
              │  only)          │
              │ .java → .class  │
              └────────┬────────┘
                       │
                       ▼
              ┌───────────────────────────────┐
              │ EXECUTION                     │
              │                                │
              │ _jspService(request,response)  │
              │    │                           │
              │    ├─ Read request attributes  │
              │    ├─ Execute scriptlets       │
              │    ├─ Evaluate expressions     │
              │    ├─ Process JSP tags         │
              │    ├─ Write HTML to out writer │
              │    └─ Flush response           │
              └───────────────────────────────┘
```

---

## 9. Database Schema

```sql
CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;

CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expenses (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    amount       DECIMAL(10, 2) NOT NULL,
    category     VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    description  TEXT,
    user_id      INT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Entity-Relationship Diagram

```
┌─────────────────┐         ┌──────────────────────────┐
│     users       │         │        expenses          │
├─────────────────┤         ├──────────────────────────┤
│ id (PK)         │◄────────┤ user_id (FK)             │
│ name            │  1:N    │ id (PK)                  │
│ email (UNIQUE)  │         │ title                    │
│ password        │         │ amount                   │
│ created_at      │         │ category                 │
└─────────────────┘         │ expense_date             │
                            │ description              │
                            │ created_at               │
                            └──────────────────────────┘
```

---

## 10. Setup & Deployment

### Prerequisites

- Java 21 JDK
- Apache Tomcat 11
- MySQL 8+
- Maven 3.9+

### Eclipse IDE Setup

1. **Import Project:**
   - File → Import → Maven → Existing Maven Projects
   - Browse to project root → Finish

2. **Update Maven Dependencies:**
   - Right-click project → Maven → Update Project
   - Check "Force Update of Snapshots/Releases"
   - This resolves all dependencies: Jakarta Servlet 6.0, MySQL Connector, Gson, JSTL

3. **Configure Tomcat:**
   - Window → Preferences → Server → Runtime Environments
   - Add Apache Tomcat 11
   - Point to Tomcat installation directory

4. **Run Application:**
   - Right-click project → Run As → Run on Server
   - Select Tomcat 11 → Finish
   - Access: `http://localhost:8080/Expense-Tracker/`

### Maven CLI Build

```bash
# Clean and build WAR
mvn clean package -DskipTests

# WAR file location
target/Expense-Tracker-0.0.1-SNAPSHOT.war

# Deploy to Tomcat webapps directory
cp target/Expense-Tracker-0.0.1-SNAPSHOT.war $TOMCAT_HOME/webapps/Expense-Tracker.war

# Or simply drop the WAR into Tomcat's webapps folder and start Tomcat
```

### Database Setup

```bash
# Execute schema.sql
mysql -u root -p < src/main/webapp/WEB-INF/schema.sql
```

Then configure credentials in `DBConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/expense_tracker";
private static final String USERNAME = "root";
private static final String PASSWORD = "0000";  // Your MySQL password
```

---

## 11. Troubleshooting

### Common Issues

| Problem | Cause | Solution |
|---------|-------|----------|
| 404 on pages | Routes not mapped | Ensure servlets have `@WebServlet("/path")` annotations |
| 401 Unauthorized | Session expired | Login again at `/login` |
| "Failed to connect to database" | MySQL not running | Start MySQL service |
| "Access denied for user" | Wrong DB credentials | Update `DBConnection.java` with correct username/password |
| Gson error on Login | Java module system blocks reflection | GsonProvider handles this with custom TypeAdapters |
| JSP not compiling | Missing JSTL dependency | Run Maven Update Project in Eclipse |
| "Cannot resolve jakarta.servlet" | Maven dependencies not downloaded | Run `mvn clean install` |
| Login/Register redirects to blank page | JSON response not handled correctly | Check browser console for errors |

### Debugging Tips

1. **Check server logs:** `$TOMCAT_HOME/logs/localhost.*.log`
2. **Browser Developer Tools:** F12 → Network tab → Check request/response
3. **Session debugging:** Check `JSESSIONID` cookie in Application tab
4. **SQL debugging:** Add `e.printStackTrace()` in catch blocks temporarily

### Eclipse-Specific Issues

1. **Project not recognized as web project:**
   - Right-click → Properties → Project Facets → Check "Dynamic Web Module" 6.0
   
2. **Server not showing in Run As:**
   - Ensure Tomcat 11 server is configured in Servers view
   
3. **Dependencies not resolving:**
   - Close project → Reopen → Maven Update

---

## Tech Stack Summary

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Web Framework | Jakarta Servlet API | 6.0 |
| View Technology | JSP (Jakarta Server Pages) | 3.1 |
| Template Tags | JSTL | 3.0 |
| JSON Library | Gson | 2.10.1 |
| Database | MySQL | 8.0+ |
| JDBC Driver | MySQL Connector/J | 8.0.33 |
| Server | Apache Tomcat | 11 |
| Build Tool | Maven | 3.9+ |
| Frontend CSS | Custom (no frameworks) | - |
| Frontend JS | Minimal (vanilla) | - |

## Architecture Patterns

| Pattern | Implementation |
|---------|---------------|
| **Layered Architecture** | Controller → Service → Repository → Database |
| **DTO Pattern** | Entity objects never exposed directly to views |
| **Repository Pattern** | Interface-based data access with DI support |
| **MVC Pattern** | Model (Entity) - View (JSP) - Controller (Servlet) |
| **Front Controller** | AuthFilter intercepts all protected routes |
| **Session Per User** | HttpSession stores authenticated User object |
| **Server-Side Rendering** | JSP generates HTML, minimal client-side JavaScript |