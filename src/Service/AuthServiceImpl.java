package Service;

import Classes.Employee;
import Classes.Roles;
import DataObject.LoginRequest;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.UUID;

public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {
    private final DatabaseService dbs;

    public AuthServiceImpl(DatabaseService dbs) throws RemoteException{
        super();
        this.dbs = dbs;
    }

    @Override
    public UUID Login(LoginRequest lr) throws RemoteException {
        // Connect with db, then compare with db with loginrequest
        // if is correct, then create and return a token.
        Employee emp = dbs.getEmployeeByIdAndPassword(lr.getUserID(), lr.getPassword());
        if(emp != null){
            UUID uid = UUID.randomUUID();

            return SessionManager.createSession(uid, emp);
        }

        return null;
    }

    public Employee Validation(UUID uid) throws RemoteException{
        return SessionManager.validate(uid);
    }

    public void validateHR(UUID uid) throws RemoteException{
        Employee emp = Validation(uid);

        if(emp.getRole().equals(Roles.EMPLOYEE)){
            throw new RemoteException("Invalid User privilege");
        }
    }

    public void endSession(UUID uid) throws RemoteException{
        SessionManager.removeSession(uid);
    }

}
