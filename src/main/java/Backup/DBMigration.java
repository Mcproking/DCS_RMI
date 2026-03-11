package Backup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBMigration {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            Statement stmt = conn.createStatement();

            stmt.execute("PRAGMA foreign_keys = ON");

            // Add IC column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE Employees ADD COLUMN IC VARCHAR(50)");
                System.out.println("Added IC column to Employees table.");
            } catch (SQLException e) {
                System.out.println("IC column already exists.");
            }

            // Add basic_salary column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE Employees ADD COLUMN basic_salary INTEGER");
                System.out.println("Added basic_salary column to Employees table.");
            } catch (SQLException e) {
                System.out.println("basic_salary column already exists.");
            }

            // Insert 3 dummy employees
            try {
                stmt.execute("INSERT INTO Employees (UserId, FirstName, LastName, password, role, leave_balance, IC, basic_salary) " +
                        "VALUES ('EMP004', 'John', 'Doe', 'emp001pass', 'EMPLOYEE', 20, '123456789012', 5000)");
                System.out.println("Inserted EMP001 - John Doe.");
            } catch (SQLException e) {
                System.out.println("EMP001 already exists or error occurred: " + e.getMessage());
            }

            try {
                stmt.execute("INSERT INTO Employees (UserId, FirstName, LastName, password, role, leave_balance, IC, basic_salary) " +
                        "VALUES ('EMP002', 'Jane', 'Smith', 'emp002pass', 'EMPLOYEE', 20, '234567890123', 5500)");
                System.out.println("Inserted EMP002 - Jane Smith.");
            } catch (SQLException e) {
                System.out.println("EMP002 already exists or error occurred: " + e.getMessage());
            }

            try {
                stmt.execute("INSERT INTO Employees (UserId, FirstName, LastName, password, role, leave_balance, IC, basic_salary) " +
                        "VALUES ('EMP003', 'Mike', 'Johnson', 'emp003pass', 'HR', 20, '345678901234', 7000)");
                System.out.println("Inserted EMP003 - Mike Johnson (HR).");
            } catch (SQLException e) {
                System.out.println("EMP003 already exists or error occurred: " + e.getMessage());
            }

            System.out.println("Migration completed successfully.");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
