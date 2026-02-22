package Service;

import Classes.*;

import javax.management.relation.Role;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseServiceImpl extends UnicastRemoteObject implements DatabaseService {
    private final Connection conn;

    public DatabaseServiceImpl() throws RemoteException {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            enableForeignKeys();
            System.out.println("Connected to SQLite");
        } catch (SQLException e) {
            throw new RemoteException("DB Fail to Init", e);
        }
    }

    private void enableForeignKeys() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    @Override
    public Employee getEmployeeByIdAndPassword(String UserID, String pw) throws RemoteException {
        String sql = """
                SELECT * FROM Employees
                WHERE UserID = ? AND password = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, UserID);
            pstmt.setString(2, pw); // ⚠ should be hashed

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setIdNumber(rs.getString("id"));
                    emp.setFirstName(rs.getString("FirstName"));
                    emp.setLastName(rs.getString("LastName"));
                    emp.setRole(Roles.valueOf(rs.getString("role")));
                    emp.setLeaveBalance(rs.getInt("leave_balance"));
                    return emp; // login success
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Employee getEmployeeById(String EmpId) throws RemoteException {
        String sql = """
                SELECT * FROM Employees
                WHERE id = ?
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,EmpId);

            try(ResultSet rs = pstmt.executeQuery()){
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setIdNumber(rs.getString("id"));
                    emp.setFirstName(rs.getString("FirstName"));
                    emp.setLastName(rs.getString("LastName"));
                    emp.setRole(Roles.valueOf(rs.getString("role")));
                    emp.setLeaveBalance(rs.getInt("leave_balance"));
                    return emp; // login success
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void updateEmployeeFirstName(String EmpID, String fn) throws RemoteException {
        String sql = """
                UPDATE Employees
                SET FirstName = ?
                WHERE id = ?
                """;
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, fn);
            pstmt.setString(2, EmpID);

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Updating First Name Failed");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void updateEmployeeLastName(String EmpID, String ln) throws RemoteException {
        String sql = """
                UPDATE Employees
                SET LastName = ?
                WHERE id =?
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, ln);
            pstmt.setString(2, EmpID);

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Updating Last Name Failed");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FamilyMember> getFamilyMemberById(String EmpID) throws RemoteException {
        String sql = """
                SELECT id, name, relationship FROM FamilyMembers
                WHERE employee_id = ?
                """;

        List<FamilyMember> _lfm = new ArrayList<>();

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, EmpID);

            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    FamilyMember fm = new FamilyMember(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("relationship")
                    );

                    _lfm.add(fm);
                }

                return _lfm;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addFamilyMemberById(String EmpID, FamilyMember fm) throws RemoteException {
        String sql = """
                INSERT INTO FamilyMembers (name, relationship, employee_id)
                VALUES (?, ?, ?)
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, fm.getName());
            pstmt.setString(2, fm.getRelationship());
            pstmt.setString(3, EmpID);

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Creating Family Member Failed");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void deleteFamilyMemberById(FamilyMember fm) throws RemoteException {
        String sql = """
                DELETE FROM FamilyMembers WHERE id = ?
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, fm.getId());

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Deleting Family Member Failed");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void addLeaveApplication(String EmpID, LeaveApplication la){
        String sql = """
                INSERT INTO LeaveHistory (leave_date, days, reason, status, employee_id)
                VALUES (?, ?, ?, ?, ?);
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setDate(1, la.getStartDateSql());        // java.sql.Date
            pstmt.setInt(2, la.getDays());
            pstmt.setString(3, la.getReason());
            pstmt.setString(4, String.valueOf(la.getStatus()));
            pstmt.setString(5, EmpID);

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Adding Leave Status Failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LeaveApplication> getLeaveApplicationById(String EmpID){
        String sql = """
            SELECT
                lh.id,
                lh.leave_date,
                lh.days,
                lh.status,
                lh.reason,
                e.UserId
            FROM LeaveHistory lh
            JOIN Employees e ON lh.employee_id = e.id
            WHERE e.id = ?
            """;
        List<LeaveApplication> _lla = new ArrayList<>();

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, EmpID);

            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    LeaveApplication la = new LeaveApplication(
                            rs.getInt("id"),
                            rs.getDate("leave_date"),
                            rs.getInt("days"),
                            rs.getString("reason"),
                            LeaveStatus.valueOf(rs.getString("status")),
                            rs.getString("UserId")
                    );

                    _lla.add(la);
                }
                return _lla;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LeaveApplication> getAllLeaveApplication(){
        String sql = """
            SELECT
            lh.id,
            lh.leave_date,
            lh.days,
            lh.status,
            lh.reason,
            e.UserId
            FROM LeaveHistory lh
            JOIN Employees e ON lh.employee_id = e.id
            """;

        List<LeaveApplication> _lla = new ArrayList<>();

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    LeaveApplication la = new LeaveApplication(
                            rs.getInt("id"),
                            rs.getDate("leave_date"),
                            rs.getInt("days"),
                            rs.getString("reason"),
                            LeaveStatus.valueOf(rs.getString("status")),
                            rs.getString("UserId")
                    );

                    _lla.add(la);
                }

                return _lla;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LeaveApplication> getAllPendingLeaveApplication(){
        String sql = """
            SELECT
            lh.id,
            lh.leave_date,
            lh.days,
            lh.status,
            lh.reason,
            e.UserId
            FROM LeaveHistory lh
            JOIN Employees e ON lh.employee_id = e.id
            WHERE lh.status = 'PENDING'
            """;

        List<LeaveApplication> _lla = new ArrayList<>();

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    LeaveApplication la = new LeaveApplication(
                            rs.getInt("id"),
                            rs.getDate("leave_date"),
                            rs.getInt("days"),
                            rs.getString("reason"),
                            LeaveStatus.valueOf(rs.getString("status")),
                            rs.getString("UserId")
                    );

                    _lla.add(la);
                }

                return _lla;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeLeaveApplication (String EmpID, LeaveApplication la){
        String sql = """
                DELETE FROM LeaveHistory WHERE id = ?
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, la.getId());

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Deleting Leave Application Failed");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void minusLeaveBalance(String EmpID, int minusDays){
        String sql = """
                UPDATE Employees
                SET leave_balance = ?
                WHERE id =?
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, minusDays);
            pstmt.setString(2, EmpID);

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Minus Leave Balance Failed");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addLeaveBalance(String EmpID, int addDays){
        String sql = """
            UPDATE Employees
            SET leave_balance = ?
            WHERE id =?
            """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, addDays);
            pstmt.setString(2, EmpID);

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Add Leave Balance Failed");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateLeaveApplication(int leaveId, LeaveStatus status) throws RemoteException {
        String sql = """
            UPDATE LeaveHistory
            SET status = ?
            WHERE id = ?
            AND status = 'PENDING'
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name()); // Convert enum to String
            ps.setInt(2, leaveId);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new RemoteException("Leave not found or already processed.");
            }

        } catch (SQLException e) {
            throw new RemoteException("Failed to update leave status", e);
        }
    }

    @Override
    public void addEmployee(Employee emp) throws RemoteException{
        String sql = """
            INSERT INTO Employees (UserId, FirstName, LastName, password, role, leave_balance)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try(PreparedStatement pstmt = conn.prepareStatement((sql))){
            pstmt.setString(1, emp.getIdNumber());
            pstmt.setString(2, emp.getFirstName());
            pstmt.setString(3, emp.getLastName());
            pstmt.setString(4, emp.getPassword());
            pstmt.setString(5, emp.getRole().name());
            pstmt.setInt(6, emp.getLeaveBalance());

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Add Employee Failed");
            }


        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Employee> getAllEmployees() throws RemoteException{
        String sql = """
            SELECT
                UserId,
                FirstName,
                LastName,
                password,
                role,
                created_at
            FROM Employees
            ORDER BY created_at DESC;
            """;

        List<Employee> _lemp = new ArrayList<>();

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    Employee emp = new Employee(
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("UserId"),
                        rs.getString("password"),
                        Roles.valueOf(rs.getString("role"))
                    );

                    _lemp.add(emp);
                }
                return _lemp;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEmployee(Employee emp) throws RemoteException{
        String sql = """
                DELETE FROM Employees WHERE UserId = ?
                """;

        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, emp.getIdNumber());

            int affectedRows = pstmt.executeUpdate();

            if(affectedRows == 0){
                throw new SQLException("Deleting Employee Failed");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
