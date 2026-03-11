package Service;

import Classes.Payroll;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface PayrollService extends Remote {
    Payroll updatePayroll(UUID token, Payroll pr) throws RemoteException;
}
