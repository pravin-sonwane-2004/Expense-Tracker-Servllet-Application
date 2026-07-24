# Expense Tracker

A full-stack Java web application for tracking personal expenses built with Jakarta Servlets, JDBC, MySQL, HTML/CSS/JavaScript.

## Architecture

```
Browser (HTML + JS)
    |
    v
Servlet (Controller)  ──→  Service (Business Logic)  ──→  DAO (Data Access)  ──→  MySQL
    |
    v
JSON Response
```

**Pattern:** MVC (Model-View-Controller) with REST-style JSON APIs
- **Model:** Java Beans (User, Expense)
- **View:** Static HTML pages with vanilla JavaScript
- **Controller:** Jakarta Servlets returning JSON
- **Service Layer:** Business logic and validation
- **DAO Layer:** Database operations via JDBC
- **Database:** MySQL

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Jakarta Servlet API | 6.0 |
| Gson | 2.10.1 |
| MySQL Connector | 8.0.33 |
| Apache Tomcat | 11 |
| Build Tool | Maven |
| Frontend | HTML5, CSS3, Vanilla JS |

## Features

### 1. Authentication
- User registration with validation (name, email format, password strength, confirm match, duplicate email check)
- Login with Remember Me cookie support
- Session management (30-minute timeout)
- Logout with session invalidation and cookie cleanup

### 2. Dashboard
- Statistics cards: Total Expenses, Highest Expense, Lowest Expense, Recent Count
- Recent expenses table (last 5 entries)
- Quick "Add Expense" button

### 3. Expense Management
- **Add Expense** - Title, Amount, Category, Date, Description
- **Edit Expense** - Pre-populated form for updates
- **Delete Expense** - With confirmation dialog
- **View Expenses** - Table with all fields
- **Search Expenses** - By title, category, or description

### 4. Categories
Default categories: Food, Fuel, Shopping, Medical, Travel, Rent, Entertainment, Others

### 5. Reports
- Monthly Expense Report (filter by month/year)
- Category-wise Expense Report
- Summary: Total Amount, Highest Expense, Lowest Expense

### 6. Profile
- View and edit profile (name, email)
- Change password (with current password verification)

### 7. Security
- Authentication filter protecting all authenticated API endpoints
- Session-based access control
- 401 response for unauthenticated requests

## Project Structure

```
Expense-Tracker/
├── src/main/java/com/expensetracker/
│   ├── controller/              # Jakarta Servlets (REST API endpoints)
│   │   ├── RegisterServlet.java  # POST /api/register
│   │   ├── LoginServlet.java     # POST /api/login
│   │   ├── DashboardServlet.java # GET /api/dashboard
│   │   ├── ExpenseServlet.java   # GET/POST /api/expenses
│   │   ├── ProfileServlet.java   # GET/POST /api/profile
│   │   └── LogoutServlet.java    # POST /api/logout
│   ├── dao/                      # Data Access Objects
│   │   ├── UserDAO.java          # User database operations
│   │   └── ExpenseDAO.java       # Expense database operations
│   ├── model/                    # Java Beans
│   │   ├── User.java             # id, name, email, password, createdAt
│   │   └── Expense.java          # id, title, amount, category, expenseDate, description, userId
│   ├── service/                  # Business Logic Layer
│   │   ├── UserService.java      # User validation + business rules
│   │   └── ExpenseService.java   # Expense validation + reports
│   ├── util/                     # Utilities
│   │   ├── DBConnection.java     # MySQL connection manager
│   │   └── GsonProvider.java     # Gson with java.time support
│   ├── filter/                   # Security Filters
│   │   └── AuthFilter.java       # Session validation for protected APIs
│   ├── listener/                 # Application Listeners
│   │   └── AppListener.java      # Startup/shutdown logging + session tracking
│   └── exception/                # Custom Exceptions
│       └── DBException.java      # Database error wrapper
├── src/main/webapp/
│   ├── index.html                # Redirects to login.html
│   ├── login.html                # Login page
│   ├── register.html             # Registration page
│   ├── dashboard.html            # Dashboard page
│   ├── expenses.html             # Expense management page
│   ├── profile.html              # Profile page
│   ├── error.html                # Error page
│   ├── css/
│   │   └── style.css             # All styles (no frameworks)
│   ├── js/
│   │   └── script.js             # Shared JavaScript utilities
│   └── WEB-INF/
│       ├── web.xml               # Deployment descriptor
│       └── schema.sql            # Database schema
├── pom.xml                       # Maven build file
└── README.md                     # This file
```

## Database Setup

### 1. Create Database and Tables

Run the SQL in `src/main/webapp/WEB-INF/schema.sql` using MySQL client:

```bash
mysql -u root -p < src/main/webapp/WEB-INF/schema.sql
```

Or manually:

```sql
CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    expense_date DATE NOT NULL,
    description TEXT,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 2. Configure Database Connection

Edit `src/main/java/com/expensetracker/util/DBConnection.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/expense_tracker";
private static final String USERNAME = "root";     // Your MySQL username
private static final String PASSWORD = "0000";      // Your MySQL password
```

## Deployment

### Option 1: Eclipse IDE
1. Import as Existing Maven Project
2. Right-click project > Run As > Run on Server
3. Select Apache Tomcat 11
4. Access at: `http://localhost:8080/Expense-Tracker/`

### Option 2: Maven + Tomcat CLI
```bash
# Build the WAR
mvn clean package

# Deploy to Tomcat
cp target/Expense-Tracker-0.0.1-SNAPSHOT.war $TOMCAT_HOME/webapps/Expense-Tracker.war

# Start Tomcat
$TOMCAT_HOME/bin/startup.sh

# Access at: http://localhost:8080/Expense-Tracker/
```

## API Endpoints

| Method | URL | Servlet | Description |
|--------|-----|---------|-------------|
| POST | `/api/register` | RegisterServlet | Register new user |
| POST | `/api/login` | LoginServlet | Login user |
| GET | `/api/dashboard` | DashboardServlet | Get dashboard stats |
| GET | `/api/expenses` | ExpenseServlet | List/search expenses |
| GET | `/api/expenses?action=edit&id=N` | ExpenseServlet | Get single expense |
| GET | `/api/expenses?action=search&keyword=X` | ExpenseServlet | Search expenses |
| GET | `/api/expenses?action=report&...` | ExpenseServlet | Generate report |
| POST | `/api/expenses` | ExpenseServlet | Add/edit/delete expense |
| GET | `/api/profile` | ProfileServlet | Get user profile |
| POST | `/api/profile` | ProfileServlet | Update profile/password |
| POST | `/api/logout` | LogoutServlet | Logout user |

## Application Flow

### Registration Flow
```
register.html  ──POST──>  RegisterServlet  ──>  UserService.validateRegistration()
                                                    │
                                                    └──>  UserDAO.registerUser()  ──>  MySQL
                                                    │
                                                    └──>  Returns JSON {success, message/error}
```

### Login Flow
```
login.html  ──POST──>  LoginServlet  ──>  UserService.validateLogin()
                                              │
                                              └──>  UserService.loginUser()
                                              │       └──>  UserDAO.loginUser()  ──>  MySQL
                                              │
                                              ├──>  Creates HttpSession
                                              ├──>  Sets Remember Me cookie (optional)
                                              └──>  Returns JSON {success, user, error}
```

### Dashboard Flow
```
dashboard.html  ──GET──>  DashboardServlet  ──>  AuthFilter (checks session)
                                                    │
                                                    └──>  ExpenseService.getTotalExpense()
                                                    │       └──>  ExpenseDAO  ──>  MySQL
                                                    │
                                                    └──>  ExpenseService.getRecentExpenses()
                                                    │       └──>  ExpenseDAO  ──>  MySQL
                                                    │
                                                    └──>  Returns JSON with stats + recent expenses
```

### Expense CRUD Flow
```
expenses.html  ──GET/POST──>  ExpenseServlet  ──>  AuthFilter (checks session)
                                                      │
                                                      ├──>  add: ExpenseService.validateExpense()
                                                      │       └──>  ExpenseDAO.addExpense()  ──>  MySQL
                                                      │
                                                      ├──>  edit: ExpenseDAO.getExpenseById()
                                                      │       └──>  Returns JSON {expense}
                                                      │
                                                      ├──>  update: ExpenseDAO.updateExpense()  ──>  MySQL
                                                      │
                                                      ├──>  delete: ExpenseDAO.deleteExpense()  ──>  MySQL
                                                      │
                                                      ├──>  search: ExpenseDAO.searchExpenses()  ──>  MySQL
                                                      │
                                                      └──>  list: ExpenseDAO.getExpensesByUserId()  ──>  MySQL
```

## Key Design Decisions

### Why HTML instead of JSP?
- Simpler frontend with static HTML + vanilla JavaScript
- REST-style JSON APIs make the backend reusable
- Clear separation of concerns (frontend/backend)
- No JSP compilation overhead

### Why Service Layer?
- Business logic separated from servlets
- Validation centralized in one place
- Easier to test and maintain
- DAO layer handles only database operations

### Why GsonProvider?
- Java 9+ module system blocks reflective access to `java.time` classes
- Custom TypeAdapters serialize `LocalDate`/`LocalDateTime` as ISO strings
- Avoids `InaccessibleObjectException` on Java 21

## Troubleshooting

### Login fails with Gson error
**Error:** `Failed making field 'java.time.LocalDateTime#date' accessible`
**Fix:** The `GsonProvider` class handles this. Rebuild and redeploy.

### 401 Unauthorized on dashboard
**Cause:** Session expired or not logged in
**Fix:** Go to login page and sign in again.

### Database connection error
**Cause:** MySQL not running or wrong credentials
**Fix:** 
1. Start MySQL service
2. Run the schema SQL
3. Check credentials in `DBConnection.java`

## Servlet Concepts Covered

- [x] Servlet Lifecycle (init, service, destroy)
- [x] HttpServlet (doGet, doPost)
- [x] Sessions (HttpSession)
- [x] Cookies (Remember Me)
- [x] Filters (AuthFilter)
- [x] Listeners (AppListener)
- [x] JDBC (Connection, PreparedStatement, ResultSet)
- [x] MVC Pattern with Service Layer
- [x] JSON Responses (Gson)
- [x] Exception Handling (DBException)
- [x] Form Validation (server-side in Service layer)
- [x] java.time API with custom Gson adapters# Expense-Tracker-Servllet-Application
