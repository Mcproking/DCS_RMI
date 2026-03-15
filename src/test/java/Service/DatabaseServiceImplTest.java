package Service;

import Classes.*;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseServiceImplTest {

    private static DatabaseServiceImpl databaseService;
    private static String originalUserDir;

    @BeforeAll
    static void beforeAll() throws Exception {
        originalUserDir = System.getProperty("user.dir");
        Path tempDir = Files.createTempDirectory("rmi-db-tests-");
        System.setProperty("user.dir", tempDir.toString());

        databaseService = new DatabaseServiceImpl();
        createSchema();
    }

    @AfterAll
    static void afterAll() {
        System.setProperty("user.dir", originalUserDir);
    }

    @BeforeEach
    void beforeEach() throws Exception {
        resetData();
    }

    @Test
    void getEmployeeAndUpdateNamesFlow() throws Exception {
        Employee employee = databaseService.getEmployeeByIdAndPassword("E001", "pw1");

        assertNotNull(employee);
        assertEquals("E001", employee.getIdNumber());
        assertEquals("Alice", employee.getFirstName());

        databaseService.updateEmployeeFirstName("E001", "Alicia");
        databaseService.updateEmployeeLastName("E001", "Tan");

        Employee updated = databaseService.getEmployeeByEmpId("E001");
        assertEquals("Alicia", updated.getFirstName());
        assertEquals("Tan", updated.getLastName());
    }

    @Test
    void familyMemberCrudFlow() throws Exception {
        FamilyMember familyMember = new FamilyMember("John", "Father");

        databaseService.addFamilyMemberById("E001", familyMember);
        List<FamilyMember> members = databaseService.getFamilyMemberById("E001");

        assertEquals(1, members.size());
        assertEquals("John", members.getFirst().getName());

        databaseService.deleteFamilyMemberById(members.getFirst());
        assertTrue(databaseService.getFamilyMemberById("E001").isEmpty());
    }

    @Test
    void leaveApplicationAndBalanceFlow() throws Exception {
        LeaveApplication leaveApplication = new LeaveApplication(new Date(), 2, "Vacation");

        databaseService.addLeaveApplication("E001", leaveApplication);
        List<LeaveApplication> applications = databaseService.getLeaveApplicationById("E001");

        assertEquals(1, applications.size());
        int leaveId = applications.getFirst().getId();
        assertNotNull(databaseService.getLeaveApplicationByLeaveId(leaveId));
        assertEquals(1, databaseService.getAllPendingLeaveApplication().size());

        databaseService.updateLeaveApplication(leaveId, LeaveStatus.APPROVED);
        assertEquals(0, databaseService.getAllPendingLeaveApplication().size());
        assertEquals(1, databaseService.getTotalLeaveApplicationByEmpID("E001"));

        databaseService.minusLeaveBalance("E001", 16);
        assertEquals(16, databaseService.getEmployeeByEmpId("E001").getLeaveBalance());
        databaseService.addLeaveBalance("E001", 20);
        assertEquals(20, databaseService.getEmployeeByEmpId("E001").getLeaveBalance());

        databaseService.removeLeaveApplication("E001", applications.getFirst());
        assertTrue(databaseService.getLeaveApplicationById("E001").isEmpty());
    }

    @Test
    void employeeAndNotificationFlow() throws Exception {
        Employee newEmployee = new Employee();
        newEmployee.setIdNumber("E002");
        newEmployee.setFirstName("Bob");
        newEmployee.setLastName("Lee");
        newEmployee.setPassword("pw2");
        newEmployee.setRole(Roles.EMPLOYEE);
        newEmployee.setLeaveBalance(10);

        databaseService.addEmployee(newEmployee);
        assertNotNull(databaseService.getEmployeeByEmpId("E002"));
        assertEquals(2, databaseService.getAllEmployees().size());

        Notification notification = new Notification("Leave approved", "E001");
        databaseService.addNotification(notification);
        List<Notification> notifications = databaseService.getAllNotificationByID("E001");

        assertEquals(1, notifications.size());
        assertFalse(notifications.getFirst().isRead());

        databaseService.markReadNotification(notifications.getFirst().getId());
        List<Notification> updatedNotifications = databaseService.getAllNotificationByID("E001");
        assertTrue(updatedNotifications.getFirst().isRead());

        databaseService.deleteEmployee(newEmployee);
        assertNull(databaseService.getEmployeeByEmpId("E002"));
    }

    private static void createSchema() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:database.db");
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS Notifications");
            statement.execute("DROP TABLE IF EXISTS LeaveHistory");
            statement.execute("DROP TABLE IF EXISTS FamilyMembers");
            statement.execute("DROP TABLE IF EXISTS Employees");

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Employees (
                    id TEXT PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
                    UserId TEXT UNIQUE NOT NULL,
                    FirstName TEXT,
                    LastName TEXT,
                    password TEXT,
                    role TEXT,
                    leave_balance INTEGER,
                    IC TEXT,
                    basic_salary INTEGER,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS FamilyMembers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    relationship TEXT,
                    employee_id TEXT,
                    FOREIGN KEY(employee_id) REFERENCES Employees(id) ON DELETE CASCADE
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS LeaveHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    leave_date DATE,
                    days INTEGER,
                    reason TEXT,
                    status TEXT,
                    employee_id TEXT,
                    FOREIGN KEY(employee_id) REFERENCES Employees(id) ON DELETE CASCADE
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS Notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    message TEXT,
                    employee_id TEXT,
                    is_read INTEGER DEFAULT 0,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
                """);
        }
    }

    private void resetData() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:database.db");
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM Notifications");
            statement.execute("DELETE FROM LeaveHistory");
            statement.execute("DELETE FROM FamilyMembers");
            statement.execute("DELETE FROM Employees");

            try (PreparedStatement insertEmployee = connection.prepareStatement("""
                INSERT INTO Employees (id, UserId, FirstName, LastName, password, role, leave_balance, IC, basic_salary)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
                insertEmployee.setString(1, "E001");
                insertEmployee.setString(2, "E001");
                insertEmployee.setString(3, "Alice");
                insertEmployee.setString(4, "Wong");
                insertEmployee.setString(5, "pw1");
                insertEmployee.setString(6, Roles.HR.name());
                insertEmployee.setInt(7, 20);
                insertEmployee.setString(8, "IC-1001");
                insertEmployee.setInt(9, 2800);
                insertEmployee.executeUpdate();
            }
        }
    }
}
