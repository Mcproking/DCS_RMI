package Service;

import Classes.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private final List<UUID> createdTokens = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (UUID token : createdTokens) {
            SessionManager.removeSession(token);
        }
    }

    @Test
    void createSessionStoresAndValidatesEmployee() {
        Employee employee = new Employee();
        employee.setIdNumber("E001");
        UUID token = UUID.randomUUID();

        UUID result = SessionManager.createSession(token, employee);
        createdTokens.add(result);

        assertEquals(token, result);
        assertSame(employee, SessionManager.validate(token));
    }

    @Test
    void createSessionReturnsExistingTokenForSameEmployee() {
        Employee first = new Employee();
        first.setIdNumber("E001");
        Employee updated = new Employee();
        updated.setIdNumber("E001");

        UUID existingToken = UUID.randomUUID();
        UUID newToken = UUID.randomUUID();
        SessionManager.createSession(existingToken, first);
        createdTokens.add(existingToken);

        UUID result = SessionManager.createSession(newToken, updated);

        assertEquals(existingToken, result);
        assertSame(updated, SessionManager.validate(existingToken));
        assertThrows(SecurityException.class, () -> SessionManager.validate(newToken));
    }

    @Test
    void removeSessionInvalidatesToken() {
        Employee employee = new Employee();
        employee.setIdNumber("E002");
        UUID token = UUID.randomUUID();
        SessionManager.createSession(token, employee);

        SessionManager.removeSession(token);

        assertThrows(SecurityException.class, () -> SessionManager.validate(token));
    }
}
