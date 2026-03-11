package Service;

import Classes.*;
import DataObject.FamilyMemberObject;
import DataObject.LeaveApplicationObject;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface EmployeeService extends Remote {

    List<FamilyMember> getFamilyMembers(UUID token) throws RemoteException;

    void addFamilyMember(FamilyMemberObject fmObj) throws  RemoteException;

    void deleteFamilyMember(FamilyMemberObject fmObj) throws RemoteException;

    void updateFirstName(UUID token, String fn) throws RemoteException;

    void updateLastName(UUID token, String ln) throws RemoteException;

    void applyLeave(LeaveApplicationObject laObj) throws RemoteException;

    List<LeaveApplication> getLeaveApplications(UUID token) throws RemoteException;

    void removeLeaveApplication(LeaveApplicationObject laObj) throws RemoteException;

    List<LeaveApplication> getAllLeaveApplication(UUID token) throws  RemoteException;

    List<LeaveApplication> getAllPendingLeaveApplication(UUID token) throws RemoteException;

    void updateLeaveApplication(UUID token, int leaveId, LeaveStatus status) throws RemoteException;

    void addEmployee(UUID token, Employee emp) throws RemoteException;

    List<Employee> getAllEmployees(UUID token) throws RemoteException;

    List<FamilyMember> getFamilyMembersByEmployeeId(UUID token, String employeeId) throws RemoteException;

    List<LeaveApplication> getLeaveApplicationsByEmployeeId(UUID token, String employeeId) throws RemoteException;

    void deleteEmployee(UUID token, Employee emp) throws RemoteException;

    void triggerNotification(Notification NotifyObj) throws RemoteException;

    List<Notification> getNotification(UUID token) throws RemoteException;

    void markAsReadNotification(UUID token, int notificationId) throws RemoteException;
}
