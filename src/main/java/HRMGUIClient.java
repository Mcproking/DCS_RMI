import Classes.*;
import DataObject.FamilyMemberObject;
import DataObject.LeaveApplicationObject;
import DataObject.LoginRequest;
import Service.AuthService;
import Service.EmployeeService;
import Service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HRMGUIClient extends JFrame {
    private AuthService authService;
    private EmployeeService employeeService;
    private PayrollService payrollService;
    private UUID token;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Employee currentEmployee;

    public HRMGUIClient() {
        super("HRM System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Connect to RMI
        try {
            authService = (AuthService) Naming.lookup("rmi://localhost:1099/login");
            employeeService = (EmployeeService) Naming.lookup("rmi://localhost:1099/employee");
            payrollService = (PayrollService) Naming.lookup("rmi://localhost:1100/payroll");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + e.getMessage(), "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Setup CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize Panels
        initLoginPanel();
        initMainMenuPanel();
        initProfilePanel();
        initLeavePanel();
        initAdminPanel();
        initNotificationPanel();
        // More panels will be initialized here later

        add(mainPanel);

        // Show login first
        cardLayout.show(mainPanel, "LOGIN");
    }

    private void initLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("HRM System Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);

        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            try {
                LoginRequest lr = new LoginRequest(username, password);
                token = authService.Login(lr);
                if (token != null) {
                    currentEmployee = authService.Validation(token);
                    updateMainMenu();
                    cardLayout.show(mainPanel, "MAIN_MENU");
                    usernameField.setText("");
                    passwordField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect User ID or Password", "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error during login: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(loginPanel, "LOGIN");
    }

    // Components for Main Menu that need to be updated
    private JLabel welcomeLabel = new JLabel();
    private JButton adminMenuBnt = new JButton("Admin Menu");

    private void initMainMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout());

        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        menuPanel.add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton profileBtn = new JButton("View Profile");
        JButton leaveBtn = new JButton("Leave Menu");
        JButton notifBtn = new JButton("Notifications");
        JButton logoutBtn = new JButton("Logout");

        buttonPanel.add(profileBtn);
        buttonPanel.add(leaveBtn);
        buttonPanel.add(notifBtn);
        buttonPanel.add(adminMenuBnt); // Admin menu is conditionally visible
        buttonPanel.add(logoutBtn);

        menuPanel.add(buttonPanel, BorderLayout.CENTER);

        // Action Listeners
        profileBtn.addActionListener(e -> {
            updateProfilePanel();
            cardLayout.show(mainPanel, "PROFILE_MENU");
        });

        leaveBtn.addActionListener(e -> {
            updateLeavePanel();
            cardLayout.show(mainPanel, "LEAVE_MENU");
        });

        notifBtn.addActionListener(e -> {
            updateNotificationPanel();
            cardLayout.show(mainPanel, "NOTIFICATION_MENU");
        });

        adminMenuBnt.addActionListener(e -> {
            updateAdminPanel();
            cardLayout.show(mainPanel, "ADMIN_MENU");
        });

        logoutBtn.addActionListener(e -> {
            try {
                authService.endSession(token);
                token = null;
                currentEmployee = null;
                cardLayout.show(mainPanel, "LOGIN");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainPanel.add(menuPanel, "MAIN_MENU");
    }

    private void updateMainMenu() {
        if (currentEmployee != null) {
            welcomeLabel.setText("Welcome, " + currentEmployee.getFirstName());
            // Show admin menu only for HR
            adminMenuBnt.setVisible(currentEmployee.getRole() == Roles.HR);
        }
    }

    // --- Profile and Family Management Panels ---

    // Profile Panel Components
    private JLabel profileFirstNameLabel = new JLabel();
    private JLabel profileLastNameLabel = new JLabel();
    private JLabel profileIdLabel = new JLabel();
    private JLabel profileRoleLabel = new JLabel();
    private JLabel profileLeaveBalanceLabel = new JLabel();
    private JLabel profileICLabel = new JLabel();
    private JLabel profileBasicSalaryLabel = new JLabel();
    private DefaultTableModel familyTableModel;
    private JTable familyTable;

    private void initProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout(10, 10));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top: Profile Info
        JPanel infoPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Profile Information"));

        infoPanel.add(new JLabel("First Name:"));
        infoPanel.add(profileFirstNameLabel);
        infoPanel.add(new JLabel("Last Name:"));
        infoPanel.add(profileLastNameLabel);
        infoPanel.add(new JLabel("ID Number:"));
        infoPanel.add(profileIdLabel);
        infoPanel.add(new JLabel("Role:"));
        infoPanel.add(profileRoleLabel);
        infoPanel.add(new JLabel("Leave Balance:"));
        infoPanel.add(profileLeaveBalanceLabel);
        infoPanel.add(new JLabel("IC:"));
        infoPanel.add(profileICLabel);
        infoPanel.add(new JLabel("Basic Salary:"));
        infoPanel.add(profileBasicSalaryLabel);

        JButton editNameBtn = new JButton("Edit Name");
        infoPanel.add(editNameBtn);
        infoPanel.add(new JLabel("")); // empty cell

        editNameBtn.addActionListener(e -> {
            String newFirstName = JOptionPane.showInputDialog(this, "Enter New First Name:",
                    currentEmployee.getFirstName());
            if (newFirstName != null && !newFirstName.trim().isEmpty()) {
                String newLastName = JOptionPane.showInputDialog(this, "Enter New Last Name:",
                        currentEmployee.getLastName());
                if (newLastName != null && !newLastName.trim().isEmpty()) {
                    try {
                        employeeService.updateFirstName(token, newFirstName);
                        employeeService.updateLastName(token, newLastName);
                        // Refresh Employee Data
                        currentEmployee = authService.Validation(token);
                        updateMainMenu();
                        updateProfilePanel();
                        JOptionPane.showMessageDialog(this, "Name updated successfully.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Failed to update name: " + ex.getMessage());
                    }
                }
            }
        });

        // Center: Family Members
        JPanel familyPanel = new JPanel(new BorderLayout(5, 5));
        familyPanel.setBorder(BorderFactory.createTitledBorder("Family Members"));

        familyTableModel = new DefaultTableModel(new String[] { "Name", "Relationship" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        familyTable = new JTable(familyTableModel);
        familyPanel.add(new JScrollPane(familyTable), BorderLayout.CENTER);

        JPanel familyBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addFamilyBtn = new JButton("Add Family");
        JButton deleteFamilyBtn = new JButton("Delete Family");
        familyBtnPanel.add(addFamilyBtn);
        familyBtnPanel.add(deleteFamilyBtn);
        familyPanel.add(familyBtnPanel, BorderLayout.SOUTH);

        addFamilyBtn.addActionListener(e -> addFamilyMemberUI());
        deleteFamilyBtn.addActionListener(e -> deleteFamilyMemberUI());

        // Bottom: Back Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back to Main Menu");
        bottomPanel.add(backBtn);

        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MAIN_MENU"));

        profilePanel.add(infoPanel, BorderLayout.NORTH);
        profilePanel.add(familyPanel, BorderLayout.CENTER);
        profilePanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(profilePanel, "PROFILE_MENU");
    }

    private void updateProfilePanel() {
        if (currentEmployee != null) {
            profileFirstNameLabel.setText(currentEmployee.getFirstName());
            profileLastNameLabel.setText(currentEmployee.getLastName());
            profileIdLabel.setText(currentEmployee.getIdNumber());
            profileRoleLabel.setText(currentEmployee.getRole().toString());
            profileLeaveBalanceLabel.setText(String.valueOf(currentEmployee.getLeaveBalance()));
            profileICLabel.setText(currentEmployee.getIC() != null ? currentEmployee.getIC() : "N/A");
            profileBasicSalaryLabel.setText(currentEmployee.getBasicSalary() > 0 ? String.valueOf(currentEmployee.getBasicSalary()) : "N/A");

            refreshFamilyTable();
        }
    }

    private void refreshFamilyTable() {
        try {
            familyTableModel.setRowCount(0);
            List<FamilyMember> family = employeeService.getFamilyMembers(token);
            if (family != null) {
                for (FamilyMember fm : family) {
                    familyTableModel.addRow(new Object[] { fm.getName(), fm.getRelationship() });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching family members: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFamilyMemberUI() {
        JTextField nameField = new JTextField(10);
        JTextField relField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
        myPanel.add(new JLabel("Name:"));
        myPanel.add(nameField);
        myPanel.add(Box.createVerticalStrut(10)); // a spacer
        myPanel.add(new JLabel("Relationship:"));
        myPanel.add(relField);

        int result = JOptionPane.showConfirmDialog(this, myPanel,
                "Add Family Member", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (nameField.getText().trim().isEmpty() || relField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
                return;
            }
            try {
                FamilyMemberObject fmObj = new FamilyMemberObject(token,
                        new FamilyMember(nameField.getText(), relField.getText()));
                employeeService.addFamilyMember(fmObj);
                refreshFamilyTable();
                JOptionPane.showMessageDialog(this, "Family member added.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding family member: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteFamilyMemberUI() {
        int selectedRow = familyTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this family member?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    List<FamilyMember> family = employeeService.getFamilyMembers(token);
                    if (selectedRow < family.size()) {
                        FamilyMemberObject fmObj = new FamilyMemberObject(token, family.get(selectedRow));
                        employeeService.deleteFamilyMember(fmObj);
                        refreshFamilyTable();
                        JOptionPane.showMessageDialog(this, "Family member deleted.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting family member: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a family member to delete.");
        }
    }

    // --- Leave Management Panel ---
    private DefaultTableModel leaveTableModel;
    private JTable leaveTable;
    private DefaultTableModel hrLeaveTableModel;
    private JTable hrLeaveTable;
    private JTabbedPane leaveTabbedPane;
    private JPanel hrLeavePanel;

    private void initLeavePanel() {
        JPanel leaveWrapperPanel = new JPanel(new BorderLayout());
        leaveWrapperPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leaveTabbedPane = new JTabbedPane();

        // --- Employee Leave View ---
        JPanel empLeavePanel = new JPanel(new BorderLayout(5, 5));
        leaveTableModel = new DefaultTableModel(new String[] { "ID", "Start Date", "Days", "Reason", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaveTable = new JTable(leaveTableModel);
        empLeavePanel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);

        JPanel empLeaveBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton applyLeaveBtn = new JButton("Apply Leave");
        JButton removeLeaveBtn = new JButton("Remove Leave");
        empLeaveBtnPanel.add(applyLeaveBtn);
        empLeaveBtnPanel.add(removeLeaveBtn);
        empLeavePanel.add(empLeaveBtnPanel, BorderLayout.SOUTH);

        applyLeaveBtn.addActionListener(e -> applyLeaveUI());
        removeLeaveBtn.addActionListener(e -> removeLeaveUI());

        leaveTabbedPane.addTab("My Leave", empLeavePanel);

        // --- HR Leave View ---
        hrLeavePanel = new JPanel(new BorderLayout(5, 5));
        hrLeaveTableModel = new DefaultTableModel(
                new String[] { "Leave ID", "Emp ID", "Start Date", "Days", "Reason", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        hrLeaveTable = new JTable(hrLeaveTableModel);
        hrLeavePanel.add(new JScrollPane(hrLeaveTable), BorderLayout.CENTER);

        JPanel hrLeaveBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approveLeaveBtn = new JButton("Approve");
        JButton rejectLeaveBtn = new JButton("Reject");
        hrLeaveBtnPanel.add(approveLeaveBtn);
        hrLeaveBtnPanel.add(rejectLeaveBtn);
        hrLeavePanel.add(hrLeaveBtnPanel, BorderLayout.SOUTH);

        approveLeaveBtn.addActionListener(e -> handleLeaveDecisionUI(LeaveStatus.APPROVED));
        rejectLeaveBtn.addActionListener(e -> handleLeaveDecisionUI(LeaveStatus.REJECTED));

        leaveWrapperPanel.add(leaveTabbedPane, BorderLayout.CENTER);

        // Bottom: Back Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back to Main Menu");
        bottomPanel.add(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MAIN_MENU"));
        leaveWrapperPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(leaveWrapperPanel, "LEAVE_MENU");
    }

    private void updateLeavePanel() {
        try {
            // Update My Leaves
            leaveTableModel.setRowCount(0);
            List<LeaveApplication> myLeaves = employeeService.getLeaveApplications(token);
            if (myLeaves != null) {
                for (LeaveApplication leaf : myLeaves) {
                    leaveTableModel.addRow(new Object[] { leaf.getId(), leaf.getStartDate(), leaf.getDays(),
                            leaf.getReason(), leaf.getStatus() });
                }
            }

            if (currentEmployee.getRole() == Roles.HR) {
                if (leaveTabbedPane.getTabCount() == 1) {
                    leaveTabbedPane.addTab("Manage Leave (HR)", hrLeavePanel);
                }
                // Refresh HR Data
                hrLeaveTableModel.setRowCount(0);
                List<LeaveApplication> allLeaves = employeeService.getAllLeaveApplication(token);
                if (allLeaves != null) {
                    for (LeaveApplication leaf : allLeaves) {
                        hrLeaveTableModel.addRow(new Object[] { leaf.getId(), leaf.getEmpID(), leaf.getStartDate(),
                                leaf.getDays(), leaf.getReason(), leaf.getStatus() });
                    }
                }
            } else {
                if (leaveTabbedPane.getTabCount() == 2) {
                    leaveTabbedPane.removeTabAt(1);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching leaves: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void applyLeaveUI() {
        JTextField startDateField = new JTextField(10);
        startDateField.setToolTipText("dd/MM/yyyy");
        JTextField daysField = new JTextField(5);
        JTextField reasonField = new JTextField(15);

        JPanel myPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        myPanel.add(new JLabel("Start Date (dd/MM/yyyy):"));
        myPanel.add(startDateField);
        myPanel.add(new JLabel("Days:"));
        myPanel.add(daysField);
        myPanel.add(new JLabel("Reason:"));
        myPanel.add(reasonField);

        int result = JOptionPane.showConfirmDialog(this, myPanel, "Apply Leave", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (startDateField.getText().trim().isEmpty() || daysField.getText().trim().isEmpty()
                    || reasonField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(startDateField.getText().trim(), formatter);
                if (localDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Start date cannot be in the past.");
                    return;
                }
                Date startDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                int days = Integer.parseInt(daysField.getText().trim());

                LeaveApplicationObject laObj = new LeaveApplicationObject(token,
                        new LeaveApplication(startDate, days, reasonField.getText().trim()));

                employeeService.applyLeave(laObj);
                updateLeavePanel();
                JOptionPane.showMessageDialog(this, "Leave applied successfully.");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use dd/MM/yyyy.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number of days.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error applying leave: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeLeaveUI() {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow >= 0) {
            String status = leaveTableModel.getValueAt(selectedRow, 4).toString();
            if (!"PENDING".equals(status)) {
                JOptionPane.showMessageDialog(this, "Only PENDING leave can be removed.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this leave application?",
                    "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    List<LeaveApplication> myLeaves = employeeService.getLeaveApplications(token);
                    if (selectedRow < myLeaves.size()) {
                        employeeService
                                .removeLeaveApplication(new LeaveApplicationObject(token, myLeaves.get(selectedRow)));
                        updateLeavePanel();
                        JOptionPane.showMessageDialog(this, "Leave application removed.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error removing leave: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a pending leave to remove.");
        }
    }

    private void handleLeaveDecisionUI(LeaveStatus decision) {
        int selectedRow = hrLeaveTable.getSelectedRow();
        if (selectedRow >= 0) {
            String status = hrLeaveTableModel.getValueAt(selectedRow, 5).toString();
            if (!"PENDING".equals(status)) {
                JOptionPane.showMessageDialog(this, "Can only decide on PENDING leave applications.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to " + decision.name() + " this leave?", "Confirm Decision",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int leaveId = (int) hrLeaveTableModel.getValueAt(selectedRow, 0);
                    employeeService.updateLeaveApplication(token, leaveId, decision);
                    updateLeavePanel();
                    JOptionPane.showMessageDialog(this, "Leave " + decision.name() + " successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating leave: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a leave application.");
        }
    }

    // --- Admin (Employee Management) Panel ---
    private DefaultTableModel empTableModel;
    private JTable empTable;
    private JComboBox<Employee> payrollEmployeeCombo;

    private void initAdminPanel() {
        JPanel adminPanel = new JPanel(new BorderLayout(10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Employee Management (HR Only)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel);
        adminPanel.add(topPanel, BorderLayout.NORTH);

        // Center: Employees Table
        empTableModel = new DefaultTableModel(new String[] { "ID Number", "First Name", "Last Name", "Role" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        empTable = new JTable(empTableModel);
        adminPanel.add(new JScrollPane(empTable), BorderLayout.CENTER);

        // Right side buttons
        JPanel rightBtnPanel = new JPanel();
        rightBtnPanel.setLayout(new BoxLayout(rightBtnPanel, BoxLayout.Y_AXIS));

        JButton addEmpBtn = new JButton("Add Employee");
        JButton deleteEmpBtn = new JButton("Delete Employee");
        JButton refreshBtn = new JButton("Refresh List");
        payrollEmployeeCombo = new JComboBox<>();
        JButton payrollBtn = new JButton("Run Payroll");
        JButton yearlyReportBtn = new JButton("Generate yearly report");

        rightBtnPanel.add(addEmpBtn);
        rightBtnPanel.add(Box.createVerticalStrut(10));
        rightBtnPanel.add(deleteEmpBtn);
        rightBtnPanel.add(Box.createVerticalStrut(10));
        rightBtnPanel.add(refreshBtn);
        rightBtnPanel.add(Box.createVerticalStrut(20));
        rightBtnPanel.add(new JLabel("Payroll (HR Only)"));
        rightBtnPanel.add(Box.createVerticalStrut(5));
        rightBtnPanel.add(payrollEmployeeCombo);
        rightBtnPanel.add(Box.createVerticalStrut(10));
        rightBtnPanel.add(payrollBtn);
        rightBtnPanel.add(Box.createVerticalStrut(20));
        rightBtnPanel.add(yearlyReportBtn);

        // Add padding to rightBtnPanel
        rightBtnPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        adminPanel.add(rightBtnPanel, BorderLayout.EAST);

        addEmpBtn.addActionListener(e -> addEmployeeUI());
        deleteEmpBtn.addActionListener(e -> deleteEmployeeUI());
        refreshBtn.addActionListener(e -> updateAdminPanel());
        yearlyReportBtn.addActionListener(e -> showYearlyReportUI());
        payrollBtn.addActionListener(e -> runPayrollForSelectedEmployeeUI());

        // Bottom: Back Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back to Main Menu");
        bottomPanel.add(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MAIN_MENU"));
        adminPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(adminPanel, "ADMIN_MENU");
    }

    private void updateAdminPanel() {
        try {
            if (currentEmployee == null || currentEmployee.getRole() != Roles.HR) {
                return;
            }
            empTableModel.setRowCount(0);
            List<Employee> allEmployees = employeeService.getAllEmployees(token);
            if (allEmployees != null) {
                for (Employee emp : allEmployees) {
                    empTableModel.addRow(
                            new Object[] { emp.getIdNumber(), emp.getFirstName(), emp.getLastName(), emp.getRole() });
                }
            }
            refreshPayrollEmployeeList(allEmployees);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching employees: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void refreshPayrollEmployeeList(List<Employee> allEmployees) {
        payrollEmployeeCombo.removeAllItems();
        if (allEmployees == null) {
            return;
        }

        for (Employee emp : allEmployees) {
            if (emp.getRole() == Roles.EMPLOYEE) {
                payrollEmployeeCombo.addItem(emp);
            }
        }
    }

    private void runPayrollForSelectedEmployeeUI() {
        Employee selectedEmp = (Employee) payrollEmployeeCombo.getSelectedItem();
        if (selectedEmp == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee for payroll.");
            return;
        }

        try {
            Payroll payrollResult = payrollService.updatePayroll(token, new Payroll(selectedEmp.getIdNumber()));
            JOptionPane.showMessageDialog(this,
                "Payroll updated for " + selectedEmp.getFirstName() + " " + selectedEmp.getLastName() + "\n"
                    + "Total Leave: " +payrollResult.getTotal_Leave() + "\n"
                    + "Deducted Salary: " + payrollResult.getDeductedSalary() + "\n"
                    + "Final Salary: " + payrollResult.getFinalSalary());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating payroll: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showYearlyReportUI() {
        if (currentEmployee == null || currentEmployee.getRole() != Roles.HR) {
            JOptionPane.showMessageDialog(this, "Only HR can generate yearly report.");
            return;
        }

        try {
            List<Employee> allEmployees = employeeService.getAllEmployees(token);
            if (allEmployees == null || allEmployees.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No employees found.");
                return;
            }

            JDialog reportDialog = new JDialog(this, "Yearly Report", true);
            reportDialog.setSize(900, 650);
            reportDialog.setLocationRelativeTo(this);
            reportDialog.setLayout(new BorderLayout(10, 10));

            JTabbedPane employeeTabs = new JTabbedPane();

            for (Employee emp : allEmployees) {
                JPanel employeeReportPanel = new JPanel(new BorderLayout());
                JTabbedPane detailsTabs = new JTabbedPane();

                detailsTabs.addTab("Profile", buildProfileReportPanel(emp));

                List<FamilyMember> familyMembers = employeeService.getFamilyMembersByEmployeeId(token,
                        emp.getIdNumber());
                detailsTabs.addTab("Family Details", buildFamilyReportPanel(familyMembers));

                List<LeaveApplication> leaveHistory = employeeService.getLeaveApplicationsByEmployeeId(token,
                        emp.getIdNumber());
                detailsTabs.addTab("Leave History", buildLeaveReportPanel(leaveHistory));

                employeeReportPanel.add(detailsTabs, BorderLayout.CENTER);
                String tabName = emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getIdNumber() + ")";
                employeeTabs.addTab(tabName, employeeReportPanel);
            }

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> reportDialog.dispose());
            bottomPanel.add(closeBtn);

            reportDialog.add(employeeTabs, BorderLayout.CENTER);
            reportDialog.add(bottomPanel, BorderLayout.SOUTH);
            reportDialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating yearly report: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildProfileReportPanel(Employee emp) {
        JPanel profilePanel = new JPanel(new GridLayout(7, 2, 8, 8));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        profilePanel.add(new JLabel("First Name:"));
        profilePanel.add(new JLabel(emp.getFirstName()));
        profilePanel.add(new JLabel("Last Name:"));
        profilePanel.add(new JLabel(emp.getLastName()));
        profilePanel.add(new JLabel("ID Number:"));
        profilePanel.add(new JLabel(emp.getIdNumber()));
        profilePanel.add(new JLabel("Role:"));
        profilePanel.add(new JLabel(emp.getRole().toString()));
        profilePanel.add(new JLabel("Leave Balance:"));
        profilePanel.add(new JLabel(String.valueOf(emp.getLeaveBalance())));
        profilePanel.add(new JLabel("IC:"));
        profilePanel.add(new JLabel(emp.getIC() != null ? emp.getIC() : "N/A"));
        profilePanel.add(new JLabel("Basic Salary:"));
        profilePanel.add(new JLabel(String.valueOf(emp.getBasicSalary())));

        return profilePanel;
    }

    private JPanel buildFamilyReportPanel(List<FamilyMember> familyMembers) {
        JPanel familyPanel = new JPanel(new BorderLayout());
        DefaultTableModel familyModel = new DefaultTableModel(new String[] { "Name", "Relationship" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (familyMembers != null) {
            for (FamilyMember member : familyMembers) {
                familyModel.addRow(new Object[] { member.getName(), member.getRelationship() });
            }
        }

        JTable familyHistoryTable = new JTable(familyModel);
        familyPanel.add(new JScrollPane(familyHistoryTable), BorderLayout.CENTER);
        return familyPanel;
    }

    private JPanel buildLeaveReportPanel(List<LeaveApplication> leaveHistory) {
        JPanel leavePanel = new JPanel(new BorderLayout());
        DefaultTableModel leaveModel = new DefaultTableModel(new String[] { "ID", "Start Date", "Days", "Reason", "Status" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (leaveHistory != null) {
            for (LeaveApplication leave : leaveHistory) {
                leaveModel.addRow(new Object[] {
                        leave.getId(),
                        leave.getStartDate(),
                        leave.getDays(),
                        leave.getReason(),
                        leave.getStatus()
                });
            }
        }

        JTable leaveHistoryTable = new JTable(leaveModel);
        leavePanel.add(new JScrollPane(leaveHistoryTable), BorderLayout.CENTER);
        return leavePanel;
    }

    private void addEmployeeUI() {
        JTextField firstNameField = new JTextField(15);
        JTextField lastNameField = new JTextField(15);
        JTextField idNumField = new JTextField(15);
        JTextField icNameField = new JTextField(20);
        JTextField leaveBalanceField = new JTextField(5);
        JTextField salaryField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JComboBox<Roles> roleCombo = new JComboBox<>(Roles.values());

        JPanel myPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        myPanel.add(new JLabel("First Name:"));
        myPanel.add(firstNameField);
        myPanel.add(new JLabel("Last Name:"));
        myPanel.add(lastNameField);
        myPanel.add(new JLabel("ID Number:"));
        myPanel.add(idNumField);
        myPanel.add(new JLabel("Password:"));
        myPanel.add(passField);
        myPanel.add(new JLabel("IC:"));
        myPanel.add(icNameField);
        myPanel.add(new JLabel("Leave Balance:"));
        myPanel.add(leaveBalanceField);
        myPanel.add(new JLabel("Basic Salary:"));
        myPanel.add(salaryField);
        myPanel.add(new JLabel("Role:"));
        myPanel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, myPanel, "Add New Employee", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String fName = firstNameField.getText().trim();
            String lName = lastNameField.getText().trim();
            String idNum = idNumField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            String ic = icNameField.getText().trim();
            int leaveBalance = Integer.parseInt(leaveBalanceField.getText().trim());
            int salary = Integer.parseInt(salaryField.getText().trim());
            Roles role = (Roles) roleCombo.getSelectedItem();

            if (fName.isEmpty() || lName.isEmpty() || idNum.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            try {
                Employee newEmp = new Employee(fName, lName, idNum, pass, role);
                newEmp.setLeaveBalance(leaveBalance);
                newEmp.setIC(ic);
                newEmp.setBasicSalary(salary);
                employeeService.addEmployee(token, newEmp);
                updateAdminPanel();
                JOptionPane.showMessageDialog(this, "Employee added successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding employee: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteEmployeeUI() {
        int selectedRow = empTable.getSelectedRow();
        if (selectedRow >= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    List<Employee> allEmployees = employeeService.getAllEmployees(token);
                    if (selectedRow < allEmployees.size()) {
                        Employee empToDelete = allEmployees.get(selectedRow);

                        if (empToDelete.getIdNumber().equals(currentEmployee.getIdNumber())) {
                            JOptionPane.showMessageDialog(this, "You cannot delete yourself.");
                            return;
                        }

                        employeeService.deleteEmployee(token, empToDelete);
                        updateAdminPanel();
                        JOptionPane.showMessageDialog(this, "Employee deleted successfully.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting employee: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
        }
    }

    // --- Notification Panel ---
    private DefaultTableModel notifTableModel;
    private JTable notifTable;

    private void initNotificationPanel() {
        JPanel notifPanel = new JPanel(new BorderLayout(10, 10));
        notifPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("My Notifications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        notifPanel.add(titleLabel, BorderLayout.NORTH);

        notifTableModel = new DefaultTableModel(new String[] { "ID", "Date", "Message", "Read Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        notifTable = new JTable(notifTableModel);
        notifPanel.add(new JScrollPane(notifTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton markReadBtn = new JButton("Mark as Read");
        btnPanel.add(markReadBtn);

        markReadBtn.addActionListener(e -> {
            int selectedRow = notifTable.getSelectedRow();
            if (selectedRow >= 0) {
                int notifId = (int) notifTableModel.getValueAt(selectedRow, 0);
                try {
                    employeeService.markAsReadNotification(token, notifId);
                    updateNotificationPanel();
                    JOptionPane.showMessageDialog(this, "Notification marked as read");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error marking notification as read: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a notification first");
            }
        });

        // Bottom: Back Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("Back to Main Menu");
        bottomPanel.add(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "MAIN_MENU"));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(btnPanel, BorderLayout.NORTH);
        southPanel.add(bottomPanel, BorderLayout.SOUTH);

        notifPanel.add(southPanel, BorderLayout.SOUTH);

        mainPanel.add(notifPanel, "NOTIFICATION_MENU");
    }

    private void updateNotificationPanel() {
        try {
            notifTableModel.setRowCount(0);
            List<Notification> notifications = employeeService.getNotification(token);
            if (notifications != null) {
                for (Notification n : notifications) {
                    notifTableModel.addRow(new Object[] {
                            n.getId(),
                            n.getCreatedAt().toString(),
                            n.getMessage(),
                            n.isRead() ? "Read" : "Unread"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching notifications: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HRMGUIClient().setVisible(true);
        });
    }
}
