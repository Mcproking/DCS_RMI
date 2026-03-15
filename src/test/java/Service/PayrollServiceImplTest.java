package Service;

import Classes.Employee;
import Classes.Payroll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PayrollServiceImplTest {

    private DatabaseService databaseService;
    private AuthService authService;
    private PayrollServiceImpl payrollService;

    @BeforeEach
    void setUp() throws RemoteException {
        databaseService = mock(DatabaseService.class);
        authService = mock(AuthService.class);
        payrollService = new PayrollServiceImpl(databaseService, authService);
    }

    @Test
    void updatePayrollCalculatesDeductionAndFinalSalary() throws Exception {
        UUID token = UUID.randomUUID();
        Payroll payroll = new Payroll("E001");
        Employee employee = new Employee();
        employee.setIdNumber("E001");
        employee.setBasicSalary(2800);

        when(databaseService.getEmployeeByEmpId("E001")).thenReturn(employee);
        when(databaseService.getTotalLeaveApplicationByEmpID("E001")).thenReturn(3);

        Payroll result = payrollService.updatePayroll(token, payroll);

        verify(authService).validateHR(token);
        assertEquals(3, result.getTotal_Leave());
        assertEquals(300, result.getDeductedSalary());
        assertEquals(2500, result.getFinalSalary());
    }

    @Test
    void updatePayrollWithNoLeaveKeepsFullSalary() throws Exception {
        UUID token = UUID.randomUUID();
        Payroll payroll = new Payroll("E010");
        Employee employee = new Employee();
        employee.setIdNumber("E010");
        employee.setBasicSalary(4200);

        when(databaseService.getEmployeeByEmpId("E010")).thenReturn(employee);
        when(databaseService.getTotalLeaveApplicationByEmpID("E010")).thenReturn(0);

        Payroll result = payrollService.updatePayroll(token, payroll);

        assertEquals(0, result.getTotal_Leave());
        assertEquals(0, result.getDeductedSalary());
        assertEquals(4200, result.getFinalSalary());
    }
}
