import Classes.*;
import DataObject.FamilyMemberObject;
import DataObject.LeaveApplicationObject;
import DataObject.LoginRequest;
import Service.AuthService;
import Service.EmployeeService;
import Service.PayrollService;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class HRMClient {
    private static AuthService authService;
    private static EmployeeService employeeService;
    private static PayrollService payrollService;
    private static UUID token;

    public static void main(String[] args) {
        try {
//            reg = LocateRegistry.getRegistry("rmi://localhost", 1099);
            authService = (AuthService) Naming.lookup("rmi://localhost:1099/login");
            employeeService = (EmployeeService) Naming.lookup("rmi://localhost:1099/employee");
            payrollService = (PayrollService) Naming.lookup("rmi://localhost:1100/payroll");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n==== SYSTEM MENU ====");
                System.out.println("1. Login");
                System.out.println("2. Exit");
                System.out.print("Select option: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        login(scanner);
                        break;
                    case "2":
                        System.out.println("System shutting down...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void login(Scanner scanner) {

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        // auth here
        LoginRequest lr = new LoginRequest(username, password);
        try{
            // assign UID from logging in
            token = authService.Login(lr);
            if(token != null){
                userMenu(scanner);
            } else {
                System.out.println("Incorrect User ID or Password");
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void userMenu(Scanner scanner) {
        Employee emp;
        try {
            while (true) {
                emp = authService.Validation(token);

                System.out.println("\n==== Welcome, " + emp.getFirstName() + " ====");
                System.out.println("==== USER MENU ====");
                System.out.println("1. View Profile");
                System.out.println("2. Leave Menu");
                System.out.println("3. Notifications");
                System.out.println("9. Extra Menu");
                System.out.println("0. Logout");
                System.out.print("Select option: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        showEmployeeProfile(scanner);
                        break;
                    case "2":
                        leaveMenu(scanner);   // 👈 CALL NEW METHOD
                        break;
                    case "3":
                        notificationMenu(scanner);
                        break;
                    case "9":
                        // Extra menu for HR
                        adminMenu(scanner);
                        break;
                    case "0":
                        System.out.println("Logging out...");
                        authService.endSession(token);
                        token = null;
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showEmployeeProfile(Scanner scanner) {
        Employee emp;
        try{
            while(true) {
                emp = authService.Validation(token);

                System.out.println("\n===== EMPLOYEE PROFILE =====");
                System.out.println("First Name  : " + emp.getFirstName());
                System.out.println("Last Name   : " + emp.getLastName());
                System.out.println("ID Number   : " + emp.getIdNumber());
                System.out.println("Role        : " + emp.getRole());
                System.out.println("Leave Balance: " + emp.getLeaveBalance());
                System.out.println("IC          : " + (emp.getIC() == null ? "N/A" : emp.getIC()));
                System.out.println("Basic Salary: " + (emp.getBasicSalary() > 0 ? emp.getBasicSalary() : "N/A"));

                System.out.println("\n1. Edit Profile");
                System.out.println("2. View Family Profile");
                System.out.println("0. Back to Main Menu");
                System.out.print("Enter choice: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        editNameMenu(scanner);
                        break;
                    case "2":
                        // enter family profile
                        manageFamily(scanner);
                        break;
                    case "0":
                        System.out.println("Returning to main menu...");
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void leaveMenu(Scanner scanner) {
        Employee emp;
        try {
            while (true) {
                emp = authService.Validation(token);

                System.out.println("\n==== LEAVE MENU ====");
                System.out.println("Remaining Leave: " + emp.getLeaveBalance());
                System.out.println("1. View Applied Leaves");
                System.out.println("2. Apply Leave");
                System.out.println("3. Remove Applied Leave");
                System.out.println("9. Extra Menu");
                System.out.println("0. Back");

                System.out.print("Choose option: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        viewAppliedLeaves();
                        break;

                    case "2":
                        applyLeave(scanner);
                        break;

                    case "3":
                        removeLeave(scanner);
                        break;

                    case "9":
                        // extra menu only for HR
                        adminLeaveMenu(scanner);
                        break;
                    case "0":
                        return;

                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    ///  This following code is for Extra Menu for Employee Management
    ///  Add, edit, and view employee
    public static void adminMenu(Scanner scanner) throws RemoteException {
        // add a auth checker here using authservice
        authService.validateHR(token);

        while (true) {
            System.out.println("\n==== ADMIN MENU ====");
            System.out.println("1. Employee Management");
            System.out.println("2. Run Payroll");
            System.out.println("3. Generate Yearly Report");
            System.out.println("0. Back");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    employeeMenu(scanner);
                    break;
                case "2":
                    runPayroll(scanner);
                    break;
                case "3":
                    generateYearReport();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    public static void employeeMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n==== EMPLOYEE MANAGEMENT ====");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. View Employee Details");
            System.out.println("0. Back");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addEmployee(scanner);
                    break;
                case "2":
                    viewEmployees();
                    break;
                case "3":
                    viewEmployeeDetails(scanner);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    ///  This section of code will be for Employee Management
    ///  Inlcude add, view, delete
    public static void addEmployee(Scanner scanner) {
        try {
            System.out.println("\n==== ADD EMPLOYEE ====");
            System.out.println("Enter 0 at any time to cancel.\n");

            String firstName = readRequiredInput(scanner, "First Name: ", true);
            if (firstName == null) return;

            String lastName = readRequiredInput(scanner, "Last Name: ", true);
            if (lastName == null) return;

            String idNum = readRequiredInput(scanner, "Id Number: ", true);
            if (idNum == null) return;

            String pass = readRequiredInput(scanner, "Password: ", true);
            if (pass == null) return;

            Roles role = selectRole(scanner);
            if (role == null) return;


            Employee emp = new Employee(firstName, lastName, idNum, pass, role);

            employeeService.addEmployee(token, emp);

            System.out.println("Employee added successfully!");
        } catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }

    public static void viewEmployeeDetails(Scanner scanner){
        List<Employee> employees = viewEmployees();
        if (employees == null || employees.isEmpty()) {
            return;
        }

        Integer selection = readMenuNumber(scanner, "Select employee to view details (0 to cancel): ", 0, employees.size());
        if (selection == null || selection == 0) {
            return;
        }

        manageEmployee(scanner, employees.get(selection - 1));
    }

    public static void manageEmployee(Scanner scanner, Employee emp) {

        while (true) {
            System.out.println("\n==== MANAGE EMPLOYEE ====");
            System.out.println("Name: " + emp.getFirstName() + " " + emp.getLastName());
            System.out.println("Role: " + emp.getRole());
            System.out.println("-------------------------");
            System.out.println("1. Delete Employee");
            System.out.println("0. Back");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    deleteEmployee(scanner, emp);
                    return; // after delete, exit menu

                case "0":
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void deleteEmployee(Scanner scanner, Employee emp) {

        try {
            System.out.println("\nAre you sure you want to delete "
                    + emp.getFirstName() + " " + emp.getLastName() + "?");
            System.out.println("1. Yes");
            System.out.println("0. Cancel");

            String confirm = scanner.nextLine();

            if (confirm.equals("1")) {
                employeeService.deleteEmployee(token, emp);
                System.out.println("Employee deleted successfully.");
            } else {
                System.out.println("Delete cancelled.");
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static Roles selectRole(Scanner scanner) {
        while (true) {
            System.out.println("==== SELECT ROLE ====");
            System.out.println("1. Employee");
            System.out.println("2. HR");
            System.out.println("0. Cancel");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    return Roles.EMPLOYEE;

                case "2":
                    return Roles.HR;

                case "0":
                    return null;   // return null if user cancels

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static List<Employee> viewEmployees() {
        List<Employee> employees = null;
        try {
            employees = employeeService.getAllEmployees(token);

            if (employees.isEmpty()) {
                System.out.println("\nNo employees found.");
                return employees;
            }

            System.out.println("\n==== EMPLOYEE LIST ====");

            for (int i = 0; i < employees.size(); i++) {
                Employee e = employees.get(i);
                System.out.println((i + 1) + ". "
                        + e.getFirstName() + " "
                        + e.getLastName()
                        + " (" + e.getRole() + ")");
            }

            return employees;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    ///  This Following code is for Applying Leave
    ///  Include View, Apply, Remove
    private static void viewAppliedLeaves() {
        try {
            List<LeaveApplication> leaves = employeeService.getLeaveApplications(token);

            if (leaves == null || leaves.isEmpty()) {
                System.out.println("No leave applications found.");
                return;
            }

            System.out.println("\n==== YOUR LEAVE APPLICATIONS ====");

            for (int i = 0; i < leaves.size(); i++) {
                LeaveApplication leave = leaves.get(i);
                System.out.println(
                        (i + 1) + ". Start Date: " + leave.getStartDate() +
                                " | Days: " + leave.getDays() +
                                " | Status: " + leave.getStatus()
                );
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyLeave(Scanner scanner) {
        try {
            System.out.println("\n==== APPLY LEAVE ====");
            System.out.println("Enter 0 at any time to cancel.");

            Date startDate;

            while (true) {
                System.out.print("Start Date (dd/MM/yyyy): ");
                String input = scanner.nextLine();

                if(input.equals("0")){
                    return;
                }

                if (!input.contains("/")) {
                    System.out.println("Invalid format. Use dd/MM/yyyy.");
                    continue;
                }

                try {
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    LocalDate localDate = LocalDate.parse(input, formatter);

                    if (localDate.isBefore(LocalDate.now())) {
                        System.out.println("Start date cannot be in the past.");
                        continue;
                    }

                    // ✅ Convert LocalDate → Date
                    startDate = Date.from(
                            localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                    );

                    break;

                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format.");
                }
            }

            Integer numberOfDays = readPositiveNumber(scanner, "Number of Days: ", true);
            if (numberOfDays == null) {
                return;
            }

            String reason = readRequiredInput(scanner, "Reason: ", true);
            if (reason == null) {
                return;
            }

            LeaveApplicationObject laObj = new LeaveApplicationObject(token,
                    new LeaveApplication(startDate, numberOfDays, reason));

            // Send this obj to empservice
            employeeService.applyLeave(laObj);

            System.out.println("Leave application submitted successfully.");

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void removeLeave(Scanner scanner) {
        try{
            List<LeaveApplication> leaves = employeeService.getLeaveApplications(token);

            if (leaves == null || leaves.isEmpty()) {
                System.out.println("No leave applications to remove.");
                return;
            }

            viewAppliedLeaves();

            Integer index = readMenuNumber(scanner, "Enter leave number to remove (0 to cancel): ", 0, leaves.size());
            if (index == null || index == 0) {
                return;
            }

            LeaveApplication selected = leaves.get(index - 1);

            if (!(selected.getStatus() == LeaveStatus.PENDING)) {
                System.out.println("Only PENDING leave can be removed.");
                return;
            }

            employeeService.removeLeaveApplication(new LeaveApplicationObject(
                    token,
                    leaves.get(index - 1)
            ));
            System.out.println("Leave removed successfully.");

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    ///  This Follwoing code is Leave Application for HR
    ///  Lisiting all, approve & rejecting
    private static void adminLeaveMenu(Scanner scanner) {
        while (true) {
            try {
                authService.validateHR(token);

                System.out.println("\n==== LEAVE MANAGEMENT MENU ====");
                System.out.println("1. List All Leave");
                System.out.println("2. Approve Leave");
                System.out.println("3. Reject Leave");
                System.out.println("0. Back");
                System.out.print("Select option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {

                    case "1":
                        listAllLeave();
                        break;

                    case "2":
                        handleLeaveDecision(scanner, token, LeaveStatus.APPROVED);
                        break;

                    case "3":
                        handleLeaveDecision(scanner, token, LeaveStatus.REJECTED);
                        break;

                    case "0":
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void listAllLeave() {
        List<LeaveApplication> leaves;

        try {
            leaves = employeeService.getAllLeaveApplication(token);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n==== LEAVE LIST ====");

        for (LeaveApplication leave : leaves) {
            System.out.println(
                    "ID: " + leave.getId() +
                            " | Employee: " + leave.getEmpID() +
                            " | Start Date: " + leave.getStartDate() +
                            " | Days: " + leave.getDays() +
                            " | Status: " + leave.getStatus()
            );
            System.out.println("Reason: " + leave.getReason());
            System.out.println("------------------------------------------------");
        }
    }

    private static List<LeaveApplication> listAllPendingLeave() {
        List<LeaveApplication> leaves;

        try {
            leaves = employeeService.getAllPendingLeaveApplication(token);

            if (leaves.isEmpty()) {
                System.out.println("No pending leave applications found.");
                return leaves;
            }

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\n==== PENDING LEAVE LIST ====");

        for (LeaveApplication leave : leaves) {
            System.out.println(
                    "ID: " + leave.getId() +
                            " | Employee: " + leave.getEmpID() +
                            " | Start Date: " + leave.getStartDate() +
                            " | Days: " + leave.getDays() +
                            " | Status: " + leave.getStatus()
            );
            System.out.println("Reason: " + leave.getReason());
            System.out.println("------------------------------------------------");
        }

        return leaves;
    }

    private static void handleLeaveDecision(Scanner scanner, UUID token, LeaveStatus status) {
        try {
            System.out.println("\n==== LEAVE APPROVAL SERVICE ====");

            List<LeaveApplication> pendingLeaves = listAllPendingLeave();

            // ✅ STOP if no leave exists
            if (pendingLeaves.isEmpty()) {
                return;
            }

            Integer leaveId = readPositiveNumber(scanner,
                    "Enter Leave ID to " + status.name().toLowerCase() + " (0 to cancel): ", true);
            if (leaveId == null) {
                return;
            }

            employeeService.updateLeaveApplication(token, leaveId, status);

            System.out.println("Leave " + status.name().toLowerCase() + " successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid leave ID format.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void notificationMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n==== NOTIFICATION MENU ====");
            System.out.println("1. View Notifications");
            System.out.println("2. Mark Notification as Read");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewNotifications();
                    break;
                case "2":
                    markNotificationAsRead(scanner);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewNotifications() {
        try {
            List<Notification> notifications = employeeService.getNotification(token);

            if (notifications == null || notifications.isEmpty()) {
                System.out.println("No notifications found.");
                return;
            }

            System.out.println("\n==== MY NOTIFICATIONS ====");
            for (Notification n : notifications) {
                System.out.println(
                        "ID: " + n.getId() +
                                " | Date: " + n.getCreatedAt() +
                                " | Status: " + (n.isRead() ? "Read" : "Unread")
                );
                System.out.println("Message: " + n.getMessage());
                System.out.println("------------------------------------------------");
            }

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void markNotificationAsRead(Scanner scanner) {
        try {
            viewNotifications();
            Integer notificationId = readPositiveNumber(scanner,
                    "Enter notification ID to mark as read (0 to cancel): ", true);
            if (notificationId == null) {
                return;
            }

            employeeService.markAsReadNotification(token, notificationId);
            System.out.println("Notification marked as read.");

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runPayroll(Scanner scanner) {
        try {
            authService.validateHR(token);

            List<Employee> employees = employeeService.getAllEmployees(token);
            if (employees == null || employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }

            System.out.println("\n==== PAYROLL EMPLOYEE LIST ====");
            int displayIndex = 1;
            for (Employee e : employees) {
                if (e.getRole() == Roles.EMPLOYEE) {
                    System.out.println(displayIndex + ". " + e.getFirstName() + " " + e.getLastName() +
                            " (" + e.getIdNumber() + ")");
                    displayIndex++;
                }
            }

            if (displayIndex == 1) {
                System.out.println("No EMPLOYEE role users available for payroll.");
                return;
            }

            Integer selection = readMenuNumber(scanner,
                    "Select employee number for payroll (0 to cancel): ", 0, displayIndex - 1);
            if (selection == null || selection == 0) {
                return;
            }

            int employeeCounter = 0;
            Employee selectedEmployee = null;
            for (Employee e : employees) {
                if (e.getRole() == Roles.EMPLOYEE) {
                    employeeCounter++;
                    if (employeeCounter == selection) {
                        selectedEmployee = e;
                        break;
                    }
                }
            }

            if (selectedEmployee == null) {
                System.out.println("Unable to find selected employee.");
                return;
            }

            Payroll payroll = payrollService.updatePayroll(token, new Payroll(selectedEmployee.getIdNumber()));
            System.out.println("\nPayroll updated for " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName());
            System.out.println("Total Leave   : " + payroll.getTotal_Leave());
            System.out.println("Deducted Salary: " + payroll.getDeductedSalary());
            System.out.println("Final Salary  : " + payroll.getFinalSalary());

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateYearReport() {
        try {
            authService.validateHR(token);

            List<Employee> employees = employeeService.getAllEmployees(token);
            if (employees == null || employees.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }

            System.out.println("\n============= YEARLY REPORT =============");

            for (Employee emp : employees) {
                System.out.println("\nEMPLOYEE: " + emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getIdNumber() + ")");
                System.out.println("Role         : " + emp.getRole());
                System.out.println("Leave Balance: " + emp.getLeaveBalance());
                System.out.println("IC           : " + (emp.getIC() == null ? "N/A" : emp.getIC()));
                System.out.println("Basic Salary : " + emp.getBasicSalary());

                List<FamilyMember> familyMembers = employeeService.getFamilyMembersByEmployeeId(token, emp.getIdNumber());
                System.out.println("\n  Family Details:");
                if (familyMembers == null || familyMembers.isEmpty()) {
                    System.out.println("  - None");
                } else {
                    for (FamilyMember member : familyMembers) {
                        System.out.println("  - " + member.getName() + " (" + member.getRelationship() + ")");
                    }
                }

                List<LeaveApplication> leaveHistory = employeeService.getLeaveApplicationsByEmployeeId(token, emp.getIdNumber());
                System.out.println("\n  Leave History:");
                if (leaveHistory == null || leaveHistory.isEmpty()) {
                    System.out.println("  - None");
                } else {
                    for (LeaveApplication leave : leaveHistory) {
                        System.out.println(
                                "  - ID: " + leave.getId() +
                                        " | Start: " + leave.getStartDate() +
                                        " | Days: " + leave.getDays() +
                                        " | Status: " + leave.getStatus()
                        );
                        System.out.println("    Reason: " + leave.getReason());
                    }
                }

                System.out.println("-----------------------------------------");
            }

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /// This Following code is for Editing User Profile
    /// Including edit
    public static void editNameMenu(Scanner scanner) {
        Employee emp;
        try {
            while (true) {
                emp = authService.Validation(token);

                System.out.println("\n===== EDIT NAME =====");
                System.out.println("Current First Name : " + emp.getFirstName());
                System.out.println("Current Last Name  : " + emp.getLastName());

                System.out.println("\n1. Edit First Name");
                System.out.println("2. Edit Last Name");
                System.out.println("0. Return to Menu");
                System.out.print("Enter choice: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        String newFirstName = readRequiredInput(scanner, "Enter new First Name: ", true);
                        if (newFirstName == null) {
                            break;
                        }
                        employeeService.updateFirstName(token, newFirstName);
                        System.out.println("First name updated successfully.");
                        break;

                    case "2":
                        String newLastName = readRequiredInput(scanner, "Enter new Last Name: ", true);
                        if (newLastName == null) {
                            break;
                        }
                        employeeService.updateLastName(token, newLastName);
                        System.out.println("Last name updated successfully.");
                        break;

                    case "0":
                        System.out.println("Returning...");
                        return;

                    default:
                        System.out.println("Invalid choice.");
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// This Following code is for Managing Family Section
    /// Including show, add, delete

    public static void manageFamily(Scanner scanner) {
        try {
            while (true) {
                System.out.println("\n===== FAMILY MANAGEMENT =====");
                displayFamilyList();

                System.out.println("\n1. Add Family Member");
                System.out.println("2. Delete Family Member");
                System.out.println("3. Return to Main Menu");
                System.out.print("Enter choice: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        addFamilyMember(scanner);
                        break;
                    case "2":
                        deleteFamilyMember(scanner);
                        break;
                    case "3":
                        System.out.println("Returning to main menu...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void displayFamilyList() throws RemoteException {

        List<FamilyMember> familyList = employeeService.getFamilyMembers(token);

        System.out.println("\n----- Family Members -----");

        if (familyList == null || familyList.isEmpty()) {
            System.out.println("No family members found.");
            return;
        }

        for (int i = 0; i < familyList.size(); i++) {
            FamilyMember member = familyList.get(i);
            System.out.println((i + 1) + ". "
                    + member.getName()
                    + " - "
                    + member.getRelationship());
        }
    }

    private static void addFamilyMember(Scanner scanner) throws RemoteException {

        String name = readRequiredInput(scanner, "Enter Name: ", true);
        if (name == null) {
            return;
        }

        String relationship = readRequiredInput(scanner, "Enter Relationship: ", true);
        if (relationship == null) {
            return;
        }

//        FamilyMember newMember = new FamilyMember(name, relationship);
        FamilyMemberObject fmObj = new FamilyMemberObject(token,
                new FamilyMember(name, relationship));

        employeeService.addFamilyMember(fmObj);

        System.out.println("Family member added successfully.");
    }

    private static void deleteFamilyMember(Scanner scanner) throws RemoteException {

        List<FamilyMember> familyList = employeeService.getFamilyMembers(token);

        if (familyList == null || familyList.isEmpty()) {
            System.out.println("No family members to delete.");
            return;
        }

        Integer index = readMenuNumber(scanner, "Enter number to delete (0 to cancel): ", 0, familyList.size());
        if (index == null || index == 0) {
            return;
        }

        if (index < 1 || index > familyList.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        employeeService.deleteFamilyMember(
                new FamilyMemberObject(token, familyList.get(index - 1))
        );
        System.out.println("Family member deleted successfully.");
    }

    private static String readRequiredInput(Scanner scanner, String prompt, boolean allowCancelWithZero) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (allowCancelWithZero && "0".equals(input)) {
                return null;
            }

            if (input.isEmpty()) {
                System.out.println("Input cannot be empty.");
                continue;
            }

            return input;
        }
    }

    private static Integer readPositiveNumber(Scanner scanner, String prompt, boolean allowCancelWithZero) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (allowCancelWithZero && "0".equals(input)) {
                return null;
            }

            try {
                int number = Integer.parseInt(input);
                if (number <= 0) {
                    System.out.println("Please enter a number greater than 0.");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static Integer readMenuNumber(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int number = Integer.parseInt(input);
                if (number < min || number > max) {
                    System.out.println("Invalid selection. Please choose between " + min + " and " + max + ".");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

}