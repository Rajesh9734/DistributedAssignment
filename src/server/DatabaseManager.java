package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Please add it to the classpath.");
            e.printStackTrace();
        }
    }
    
    // SQLite connection string
    private static final String URL = "jdbc:sqlite:hrm_system.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create Users table
            // Role: "HR", "EMP"
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY, " +
                    "email TEXT UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL" +
                    ");";
            stmt.execute(createUsersTable);

            // Create Employees table
            // Added designation, address
            String createEmployeesTable = "CREATE TABLE IF NOT EXISTS employees (" +
                    "id TEXT PRIMARY KEY, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "ic_passport TEXT, " +
                    "designation TEXT, " + // New
                    "address TEXT, " + // New
                    "leave_balance INTEGER DEFAULT 20, " +
                    "FOREIGN KEY(id) REFERENCES users(id)" +
                    ");";
            stmt.execute(createEmployeesTable);

            // Create FamilyMembers table (One-to-Many)
            // Replaces old family_details table
            String createFamilyTable = "CREATE TABLE IF NOT EXISTS family_members (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "employee_id TEXT, " +
                    "first_name TEXT, " +
                    "last_name TEXT, " +
                    "relation TEXT, " +
                    "email TEXT, " +
                    "phone TEXT, " +
                    "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                    ");";
            stmt.execute(createFamilyTable);

            // Create LeaveApplications table
            String createLeaveTable = "CREATE TABLE IF NOT EXISTS leave_applications (" +
                    "leave_id TEXT PRIMARY KEY, " +
                    "employee_id TEXT, " +
                    "start_date TEXT, " +
                    "end_date TEXT, " +
                    "status TEXT, " +
                    "reason TEXT, " +
                    "year INTEGER, " +
                    "FOREIGN KEY(employee_id) REFERENCES employees(id)" +
                    ");";
            stmt.execute(createLeaveTable);

            try {
                stmt.execute("ALTER TABLE leave_applications ADD COLUMN reason TEXT");
            } catch (SQLException ignore) {
                // Column already exists
            }

            // Insert default HR admin if not exists
            // ID: admin, Pass: admin123
            String checkAdmin = "SELECT id FROM users WHERE id = 'admin'";
            if (!stmt.executeQuery(checkAdmin).next()) {
                String insertAdmin = "INSERT INTO users (id, email, password, role) VALUES ('admin', 'admin@hrm.com', 'admin123', 'HR')";
                stmt.execute(insertAdmin);
                System.out.println("Default Admin created: ID=admin, Email=admin@hrm.com, Pass=admin123");
            }
            
            System.out.println("Database and tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
