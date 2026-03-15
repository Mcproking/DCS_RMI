package Service;

import Classes.Employee;
import Classes.Roles;
import DataObject.LoginRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private DatabaseService databaseService;
    private AuthServiceImpl authService;
    private final List<UUID> createdTokens = new ArrayList<>();

    @BeforeEach
    void setUp() throws RemoteException {
        databaseService = mock(DatabaseService.class);
        authService = new AuthServiceImpl(databaseService);
    }

    @AfterEach
    void tearDown() {
        for (UUID token : createdTokens) {
            SessionManager.removeSession(token);
        }
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() throws Exception {
        Employee employee = new Employee();
        employee.setIdNumber("E001");
        employee.setRole(Roles.HR);

        when(databaseService.getEmployeeByIdAndPassword("E001", "pw")).thenReturn(employee);

        UUID token = authService.Login(new LoginRequest("E001", "pw"));
        createdTokens.add(token);

        assertNotNull(token);
        assertSame(employee, SessionManager.validate(token));
        verify(databaseService).getEmployeeByIdAndPassword("E001", "pw");
    }

    @Test
    void loginReturnsNullWhenCredentialsAreInvalid() throws Exception {
        when(databaseService.getEmployeeByIdAndPassword("E001", "wrong")).thenReturn(null);

        UUID token = authService.Login(new LoginRequest("E001", "wrong"));

        assertNull(token);
    }

    @Test
    void validationReturnsEmployeeFromSession() throws Exception {
        Employee employee = new Employee();
        employee.setIdNumber("E001");
        UUID token = UUID.randomUUID();
        SessionManager.createSession(token, employee);
        createdTokens.add(token);

        Employee validated = authService.Validation(token);

        assertSame(employee, validated);
    }

    @Test
    void validateHRThrowsForEmployeeRole() throws Exception {
        Employee employee = new Employee();
        employee.setIdNumber("E002");
        employee.setRole(Roles.EMPLOYEE);
        UUID token = UUID.randomUUID();
        SessionManager.createSession(token, employee);
        createdTokens.add(token);

        assertThrows(RemoteException.class, () -> authService.validateHR(token));
    }

    @Test
    void validateHRAllowsHRRole() throws Exception {
        Employee employee = new Employee();
        employee.setIdNumber("E003");
        employee.setRole(Roles.HR);
        UUID token = UUID.randomUUID();
        SessionManager.createSession(token, employee);
        createdTokens.add(token);

        assertDoesNotThrow(() -> authService.validateHR(token));
    }

    @Test
    void endSessionRemovesToken() throws Exception {
        Employee employee = new Employee();
        employee.setIdNumber("E004");
        UUID token = UUID.randomUUID();
        SessionManager.createSession(token, employee);

        authService.endSession(token);

        assertThrows(SecurityException.class, () -> SessionManager.validate(token));
    }
}
