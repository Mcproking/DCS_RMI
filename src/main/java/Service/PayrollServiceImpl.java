package Service;

import Classes.Employee;
import Classes.Payroll;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class PayrollServiceImpl extends UnicastRemoteObject implements PayrollService {
    private final DatabaseService dbs;
    private final AuthService as;

    public PayrollServiceImpl(DatabaseService dbs, AuthService as) throws RemoteException{
        super();
        this.dbs = dbs;
        this.as = as;
    }

    @Override
    public Payroll updatePayroll(UUID token, Payroll pr) throws RemoteException {
        as.validateHR(token);

        // get data from dbs, then generate final salary and add the deducted salary
        Employee emp = dbs.getEmployeeByEmpId(pr.getEmpID());
        pr.setTotal_Leave(dbs.getTotalLeaveApplicationByEmpID(emp.getIdNumber()));

        int MONTH_MAX_DAY = 28;

        // month max day = 28
        int dailySalary = emp.getBasicSalary() / MONTH_MAX_DAY;
        pr.setDeductedSalary(dailySalary * pr.getTotal_Leave());
        pr.setFinalSalary(emp.getBasicSalary() - pr.getDeductedSalary());

        return pr;
    }
}
