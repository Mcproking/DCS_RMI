import Classes.*;
import DataObject.FamilyMemberObject;
import DataObject.LeaveApplicationObject;
import DataObject.LoginRequest;
import Service.AuthService;
import Service.EmployeeService;

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
    private static UUID token;

    public static void main(String[] args) {
        try {
//            reg = LocateRegistry.getRegistry("rmi://localhost", 1099);
            authService = (AuthService) Naming.lookup("rmi://localhost:1099/login");
            employeeService = (EmployeeService) Naming.lookup("rmi://localhost:1099/employee");
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
            System.out.println("2. Generate Yearly Report");
            System.out.println("0. Back");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    employeeMenu(scanner);
                    break;
                case "2":
//                    generateYearReport(scanner);
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

            System.out.print("First Name: ");
            String firstName = scanner.nextLine();
            if (firstName.equals("0")) return;

            System.out.print("Last Name: ");
            String lastName = scanner.nextLine();
            if (lastName.equals("0")) return;

            System.out.print("Id Number: ");
            String idNum = scanner.nextLine();
            if (idNum.equals("0")) return;

            System.out.print("Password: ");
            String pass = scanner.nextLine();
            if (pass.equals("0")) return;

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
        System.out.print("Select employee to view details: ");

        String choice = scanner.nextLine();

        if (choice.equals("0")) return;
        if (employees.isEmpty()) return;

        try {
            int index = Integer.parseInt(choice) - 1;

            if (index >= 0 && index < employees.size()) {
                manageEmployee(scanner, employees.get(index));
            } else {
                System.out.println("Invalid selection.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
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

            System.out.print("Number of Days: ");
            int numberOfDays = Integer.parseInt(scanner.nextLine());

            System.out.print("Reason: ");
            String reason = scanner.nextLine();

            LeaveApplicationObject laObj = new LeaveApplicationObject(token,
                    new LeaveApplication(startDate, numberOfDays, reason));

            // Send this obj to empservice
            employeeService.applyLeave(laObj);

            System.out.println("Leave application submitted successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid number of days.");
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

            System.out.print("Enter leave number to remove (0 to cancel): ");
            String input = scanner.nextLine();

            try {
                int index = Integer.parseInt(input);

                if (index == 0) {
                    return;
                }

                if (index < 1 || index > leaves.size()) {
                    System.out.println("Invalid selection.");
                    return;
                }

                LeaveApplication selected = leaves.get(index - 1);

                // Optional: Only allow removal if status is PENDING
                if (!(selected.getStatus() == LeaveStatus.PENDING)) {
                    System.out.println("Only PENDING leave can be removed.");
                    return;
                }

                // remove leave from empservice
                employeeService.removeLeaveApplication(new LeaveApplicationObject(
                        token,
                        leaves.remove(index - 1)
                ));
                System.out.println("Leave removed successfully.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }

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

                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {

                    case 1:
                        listAllLeave();
                        break;

                    case 2:
                        handleLeaveDecision(scanner, token, LeaveStatus.APPROVED);
                        break;

                    case 3:
                        handleLeaveDecision(scanner, token, LeaveStatus.REJECTED);
                        break;

                    case 0:
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

            System.out.print("Enter Leave ID to " + status.name().toLowerCase() + ": ");
            int leaveId = Integer.parseInt(scanner.nextLine());

            employeeService.updateLeaveApplication(token, leaveId, status);

            System.out.println("Leave " + status.name().toLowerCase() + " successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid leave ID format.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
                        System.out.print("Enter new First Name: ");
                        String newFirstName = scanner.nextLine();
                        employeeService.updateFirstName(token, newFirstName);
                        System.out.println("First name updated successfully.");
                        break;

                    case "2":
                        System.out.print("Enter new Last Name: ");
                        String newLastName = scanner.nextLine();
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

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Relationship: ");
        String relationship = scanner.nextLine();

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

        System.out.print("Enter number to delete: ");
        int index = scanner.nextInt();

        if (index < 1 || index > familyList.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        employeeService.deleteFamilyMember(familyList.get(index - 1));
        System.out.println("Family member deleted successfully.");
    }

}