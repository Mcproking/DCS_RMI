package Service;

import Classes.Employee;
import DataObject.LoginRequest;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface AuthService extends Remote {
    // Login Request Service
    UUID Login(LoginRequest loginRequest) throws RemoteException;

    Employee Validation(UUID uid) throws  RemoteException;

    void validateHR(UUID uid) throws RemoteException;

    void endSession(UUID uid) throws RemoteException;
}
