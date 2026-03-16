package Backup;

import Classes.Roles;

import java.sql.*;

public class DatabaseCreate {
    public static void createEmployeeTable(Connection conn){
        String sql = """
                CREATE TABLE IF NOT EXISTS Employees (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserId VARCHAR(100) NOT NULL UNIQUE,
                    FirstName VARCHAR(100) NOT NULL,
                    LastName VARCHAR(100) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    leave_balance INTEGER DEFAULT 0,
                    IC VARCHAR(50),
                    basic_salary INTEGER,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try(Statement stmt = conn.createStatement()){
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void createFamilyMember(Connection conn){
        String sql = """
                CREATE TABLE IF NOT EXISTS FamilyMembers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL,
                    relationship VARCHAR(50) NOT NULL,
                    employee_id VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                
                    FOREIGN KEY (employee_id)\s
                        REFERENCES Employees(UserId)
                        ON DELETE CASCADE
                );
                """;

        try(Statement stmt = conn.createStatement()){
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createLeave(Connection conn) {
        String sql = """
                CREATE TABLE IF NOT EXISTS LeaveHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    leave_date DATE NOT NULL,
                    days INTEGER NOT NULL,
                    reason VARCHAR(255),
                    status VARCHAR(50) NOT NULL,
                    employee_id VARCHAR(50) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                
                    FOREIGN KEY (employee_id)\s
                        REFERENCES Employees(UserId)
                        ON DELETE CASCADE
                );
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int createHR(String uid, String fn, String ln, String pass, Connection conn, Roles role, int leaveBalance, String IC, int basic_salary){
        String sql = """
                INSERT INTO Employees (UserId, FirstName, LastName, password, role, leave_balance, ic, basic_salary)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, uid);
            pstmt.setString(2, fn);
            pstmt.setString(3, ln);
            pstmt.setString(4, pass); // ⚠ Should be hashed before storing
            pstmt.setString(5, role.toString());
            pstmt.setInt(6, leaveBalance);
            pstmt.setString(7, IC);
            pstmt.setInt(8, basic_salary);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            // Get auto-generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // return new employee ID
                } else {
                    throw new SQLException("Creating employee failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

        public static void main(String[] args){
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            // enable forgein key
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys= ON");

            // create table
            createEmployeeTable(conn);
            createFamilyMember(conn);
            createLeave(conn);

            // add 1 HR sample
            int temp = createHR("HR001", "HR", "Admin", "HR", conn, Roles.HR, 20, "112233445566778899", 2000);
            int temp2 = createHR("EMP001", "EMP", "Work", "EMP", conn, Roles.EMPLOYEE, 20, "998877665544332211", 1500);
            System.out.println("USerid: " + temp);
            System.out.println("USerid: " + temp2);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


}
