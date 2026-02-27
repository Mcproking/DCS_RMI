package Service;

import Classes.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DatabaseService extends Remote {
    Employee getEmployeeByIdAndPassword(String EmpID, String pw) throws RemoteException;

    Employee getEmployeeById(String EmpId) throws RemoteException;

    List<FamilyMember> getFamilyMemberById(String EmpID) throws  RemoteException;

    void addFamilyMemberById(String EmpID, FamilyMember fm) throws RemoteException;

    void deleteFamilyMemberById(FamilyMember fm) throws RemoteException;

    void updateEmployeeFirstName(String EmpID, String fn) throws RemoteException;

    void updateEmployeeLastName(String EmpID, String ln) throws RemoteException;

    void addLeaveApplication(String EmpID, LeaveApplication la) throws RemoteException;

    List<LeaveApplication> getLeaveApplicationById(String EmpID) throws RemoteException;

    LeaveApplication getLeaveApplicationByLeaveId(int LeaveId) throws RemoteException;

    void minusLeaveBalance(String EmpID, int minusDays) throws RemoteException;

    void addLeaveBalance(String EmpID, int addDays) throws RemoteException;

    void removeLeaveApplication(String EmpID, LeaveApplication la) throws RemoteException;

    List<LeaveApplication> getAllLeaveApplication() throws RemoteException;

    List<LeaveApplication> getAllPendingLeaveApplication() throws RemoteException;

    void updateLeaveApplication(int leaveId, LeaveStatus status) throws RemoteException;

    void addEmployee(Employee emp)throws RemoteException;

    List<Employee> getAllEmployees() throws RemoteException;

    void deleteEmployee(Employee emp) throws RemoteException;

    void addNotification(Notification NotifyObj) throws RemoteException;

    List<Notification> getAllNotificationByID(String EmpID) throws RemoteException;

    void markReadNotification(int NotificationId) throws RemoteException;
}
