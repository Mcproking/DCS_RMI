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

            String sql = "CREATE TABLE IF NOT EXISTS Notifications (\n" +
                    "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    message TEXT NOT NULL,\n" +
                    "    employee_id VARCHAR(50) NOT NULL,\n" +
                    "    is_read BOOLEAN DEFAULT 0,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    FOREIGN KEY (employee_id) REFERENCES Employees(UserId) ON DELETE CASCADE\n" +
                    ");";
            stmt.execute(sql);
            System.out.println("Notifications table created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
