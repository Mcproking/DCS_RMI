package Service;

import Classes.*;
import DataObject.FamilyMemberObject;
import DataObject.LeaveApplicationObject;

import java.nio.file.attribute.UserPrincipalLookupService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;

public class EmployeeServiceImpl extends UnicastRemoteObject implements EmployeeService {
    private final DatabaseService dbs;
    private final AuthService as;

    public EmployeeServiceImpl(DatabaseService dbService, AuthService as) throws RemoteException{
        super();
        this.dbs = dbService;
        this.as = as;
    }


    @Override
    public void updateFirstName(UUID token, String fn) throws RemoteException {
        Employee emp = SessionManager.validate(token);

        dbs.updateEmployeeFirstName(emp.getIdNumber(),fn);
        emp.setFirstName(fn);

    }

    @Override
    public void updateLastName(UUID token, String ln) throws RemoteException {
        Employee emp = SessionManager.validate(token);

        dbs.updateEmployeeLastName(emp.getIdNumber(), ln);
        emp.setLastName(ln);
    }

    @Override
    public List<FamilyMember> getFamilyMembers(UUID token){
        Employee emp = SessionManager.validate(token);

        try {
//            List<FamilyMember> ListFM = dbs.getFamilyMemberById(emp.getIdNumber());
            return dbs.getFamilyMemberById(emp.getIdNumber());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFamilyMember(FamilyMemberObject fmObj) throws RemoteException {
        Employee emp = SessionManager.validate(fmObj.getToken());

        dbs.addFamilyMemberById(emp.getIdNumber(), fmObj.getFm());
    }

    @Override
    public void deleteFamilyMember(FamilyMember fm) throws RemoteException {
//        Employee emp = SessionManager.validate(token);

        dbs.deleteFamilyMemberById(fm);
    }

    @Override
    public void applyLeave(LeaveApplicationObject laObj) throws RemoteException {
        Employee emp = SessionManager.validate(laObj.getToken());

        // if got enough leave days
        if(emp.getLeaveBalance() >= laObj.getLeaveApplication().getDays()){
            dbs.addLeaveApplication(emp.getIdNumber(), laObj.getLeaveApplication());
            // dbs minus emp days
            dbs.minusLeaveBalance(emp.getIdNumber(),emp.getLeaveBalance() - laObj.getLeaveApplication().getDays());
            // minus locally
            emp.setLeaveBalance(emp.getLeaveBalance() - laObj.getLeaveApplication().getDays());
        } else {
            throw new RuntimeException("Not Enough Days");
        }

    }

    @Override
    public List<LeaveApplication> getLeaveApplications(UUID token) throws RemoteException{
        Employee emp = SessionManager.validate(token);

        try{
            return dbs.getLeaveApplicationById(emp.getIdNumber());
        } catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeLeaveApplication(LeaveApplicationObject laObj) throws RemoteException{
        Employee emp = SessionManager.validate(laObj.getToken());

        // remove leave
        dbs.removeLeaveApplication(emp.getIdNumber(), laObj.getLeaveApplication());
        // add leave
        dbs.addLeaveBalance(emp.getIdNumber(), laObj.getLeaveApplication().getDays());
    }

    @Override
    public List<LeaveApplication> getAllLeaveApplication(UUID token) throws RemoteException{
        Employee emp = SessionManager.validate(token);

        if(emp.getRole().equals(Roles.EMPLOYEE)){
            throw new RuntimeException("Invalid User privilege");
        }

        return dbs.getAllLeaveApplication();
    }

    @Override
    public void updateLeaveApplication(UUID token, int leaveId, LeaveStatus status) throws RemoteException{
        Employee emp = SessionManager.validate(token);

        if(emp.getRole().equals(Roles.EMPLOYEE)){
            throw new RuntimeException("Invalid User privilege");
        }

        dbs.updateLeaveApplication(leaveId, status);
    }

    @Override
    public List<LeaveApplication> getAllPendingLeaveApplication(UUID token) throws RemoteException{
        Employee emp = SessionManager.validate(token);

        if(emp.getRole().equals(Roles.EMPLOYEE)){
            throw new RuntimeException("Invalid User privilege");
        }

        return dbs.getAllPendingLeaveApplication();
    }

    @Override
    public void addEmployee(UUID token, Employee newEmp) throws RemoteException{
        Employee emp = SessionManager.validate(token);

        if(emp.getRole().equals(Roles.EMPLOYEE)){
            throw new RuntimeException("Invalid User privilege");
        }

        dbs.addEmployee(newEmp);
    }

    @Override
    public List<Employee> getAllEmployees(UUID token) throws RemoteException{
        Employee emp = SessionManager.validate(token);
        as.validateHR(token);

        return dbs.getAllEmployees();
    }

    @Override
    public void deleteEmployee(UUID token, Employee emp) throws RemoteException{
        as.validateHR(token);

        dbs.deleteEmployee(emp);
    }
}
