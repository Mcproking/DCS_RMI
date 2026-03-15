package Service;

import Classes.*;
import DataObject.LeaveApplicationObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    private DatabaseService databaseService;
    private AuthService authService;
    private EmployeeServiceImpl employeeService;
    private final List<UUID> createdTokens = new ArrayList<>();

    @BeforeEach
    void setUp() throws RemoteException {
        databaseService = mock(DatabaseService.class);
        authService = mock(AuthService.class);
        employeeService = new EmployeeServiceImpl(databaseService, authService);
    }

    @AfterEach
    void tearDown() {
        for (UUID token : createdTokens) {
            SessionManager.removeSession(token);
        }
    }

    @Test
    void updateFirstNameUpdatesDatabaseAndSessionEmployee() throws Exception {
        Employee employee = sessionEmployee("E001", Roles.EMPLOYEE, 12);

        employeeService.updateFirstName(createdTokens.getFirst(), "Alice");

        verify(databaseService).updateEmployeeFirstName("E001", "Alice");
        assertEquals("Alice", employee.getFirstName());
    }

    @Test
    void applyLeaveWithEnoughBalanceUpdatesDbAndBalance() throws Exception {
        Employee employee = sessionEmployee("E002", Roles.EMPLOYEE, 10);
        UUID token = createdTokens.getFirst();
        LeaveApplication leaveApplication = new LeaveApplication(new Date(), 3, "Vacation");

        employeeService.applyLeave(new LeaveApplicationObject(token, leaveApplication));

        verify(databaseService).addLeaveApplication("E002", leaveApplication);
        verify(databaseService).minusLeaveBalance("E002", 7);
        assertEquals(7, employee.getLeaveBalance());
    }

    @Test
    void applyLeaveWithInsufficientBalanceThrows() throws Exception {
        Employee employee = sessionEmployee("E003", Roles.EMPLOYEE, 1);
        UUID token = createdTokens.getFirst();
        LeaveApplication leaveApplication = new LeaveApplication(new Date(), 3, "Vacation");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.applyLeave(new LeaveApplicationObject(token, leaveApplication)));

        assertEquals("Not Enough Days", ex.getMessage());
        verify(databaseService, never()).addLeaveApplication(anyString(), any());
        verify(databaseService, never()).minusLeaveBalance(anyString(), anyInt());
        assertEquals(1, employee.getLeaveBalance());
    }

    @Test
    void getAllLeaveApplicationRejectsEmployeeRole() throws Exception {
        sessionEmployee("E004", Roles.EMPLOYEE, 5);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> employeeService.getAllLeaveApplication(createdTokens.getFirst()));

        assertEquals("Invalid User privilege", ex.getMessage());
    }

    @Test
    void getAllLeaveApplicationReturnsListForHrRole() throws Exception {
        sessionEmployee("H001", Roles.HR, 5);
        List<LeaveApplication> expected = List.of(new LeaveApplication(new Date(), 2, "Medical"));
        when(databaseService.getAllLeaveApplication()).thenReturn(expected);

        List<LeaveApplication> actual = employeeService.getAllLeaveApplication(createdTokens.getFirst());

        assertEquals(expected, actual);
    }

    @Test
    void getAllEmployeesValidatesHrAndReturnsDatabaseResult() throws Exception {
        sessionEmployee("H002", Roles.HR, 5);
        List<Employee> expected = List.of(new Employee());
        when(databaseService.getAllEmployees()).thenReturn(expected);

        List<Employee> actual = employeeService.getAllEmployees(createdTokens.getFirst());

        verify(authService).validateHR(createdTokens.getFirst());
        assertEquals(expected, actual);
    }

    @Test
    void updateLeaveApplicationTriggersNotificationWithStatusMessage() throws Exception {
        sessionEmployee("H003", Roles.HR, 5);
        LeaveApplication leaveApplication = new LeaveApplication(10, new Date(), 2, "Trip", LeaveStatus.PENDING, "E777");
        when(databaseService.getLeaveApplicationByLeaveId(10)).thenReturn(leaveApplication);

        employeeService.updateLeaveApplication(createdTokens.getFirst(), 10, LeaveStatus.APPROVED);

        verify(databaseService).updateLeaveApplication(10, LeaveStatus.APPROVED);
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(databaseService).addNotification(notificationCaptor.capture());
        Notification sent = notificationCaptor.getValue();
        assertEquals("Your leave has been approved.", sent.getMessage());
        assertEquals("E777", sent.getEmployeeId());
    }

    @Test
    void getNotificationUsesSessionEmployeeId() throws Exception {
        Employee employee = sessionEmployee("E009", Roles.EMPLOYEE, 8);
        List<Notification> expected = List.of(new Notification("Hello", "E009"));
        when(databaseService.getAllNotificationByID("E009")).thenReturn(expected);

        List<Notification> actual = employeeService.getNotification(createdTokens.getFirst());

        assertEquals(expected, actual);
        assertEquals("E009", employee.getIdNumber());
        verify(databaseService).getAllNotificationByID("E009");
    }

    private Employee sessionEmployee(String id, Roles role, int leaveBalance) {
        Employee employee = new Employee();
        employee.setIdNumber(id);
        employee.setRole(role);
        employee.setLeaveBalance(leaveBalance);
        UUID token = UUID.randomUUID();
        SessionManager.createSession(token, employee);
        createdTokens.add(token);
        return employee;
    }
}
