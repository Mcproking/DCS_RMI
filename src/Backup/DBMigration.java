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

            String sql = """
                CREATE TABLE IF NOT EXISTS Notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    message TEXT NOT NULL,
                    employee_id VARCHAR(50) NOT NULL,
                    is_read INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (employee_id) REFERENCES Employees(id) ON DELETE CASCADE ON UPDATE CASCADE
                );
                """;
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute(sql);
            System.out.println("Notifications table created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
