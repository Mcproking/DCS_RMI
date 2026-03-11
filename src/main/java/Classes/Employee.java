package Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    private String idNumber;
    private String password;
    private Roles role; // "HR" or "Employee"
    private List<FamilyMember> familyDetails;
    private List<LeaveApplication> leaveHistory;
    private int leaveBalance;

    public Employee() {
    }

    public Employee(String firstName, String lastName, String idNumber, String password, Roles role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;
        this.password = password;
        this.role = role;
        this.familyDetails = new ArrayList<>();
        this.leaveHistory = new ArrayList<>();
        this.leaveBalance = 20;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String id) {
        this.idNumber = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<FamilyMember> getFamilyDetails() {
        return familyDetails;
    }

    public void setFamilyDetails(List<FamilyMember> family) {
        this.familyDetails = family;
    }

    public List<LeaveApplication> getLeaveHistory() {
        return leaveHistory;
    }

    public void addLeaveApplication(LeaveApplication la) {
        this.leaveHistory.add(la);
    }

    public int getLeaveBalance() {
        return leaveBalance;
    }

    public void setLeaveBalance(int balance) {
        this.leaveBalance = balance;
    }

    @Override
    public String toString() {
        return String.format("Employee: %s %s (%s)", firstName, lastName, idNumber);
    }
}
