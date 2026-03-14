package server;

import common.Employee;
import common.FamilyMember;
import common.HRMInterface;
import common.LeaveApplication;
import common.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HRMServerImpl extends UnicastRemoteObject implements HRMInterface {

    protected HRMServerImpl() throws RemoteException {
        super();
        DatabaseManager.initializeDatabase();
    }

    @Override
    public User login(String email, String password) throws RemoteException {
        String sql = "SELECT id, email, password, role FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                String id = rs.getString("id");
                if ("EMP".equals(role)) {
                    // Fetch full employee details
                    return getFullEmployeeDetails(id, email, password, role);
                } else {
                    return new User(id, email, password, role);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Database error during login", e);
        }
        return null; // Login failed
    }
    
    private Employee getFullEmployeeDetails(String id, String email, String password, String role) {
        Employee emp = null;
        String empSql = "SELECT * FROM employees WHERE id = ?";
        String famSql = "SELECT * FROM family_members WHERE employee_id = ?"; // UPDATED TABLE
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(empSql)) {
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    emp = new Employee(id, email, password, role, 
                            rs.getString("first_name"), 
                            rs.getString("last_name"), 
                            rs.getString("ic_passport"),
                            rs.getString("designation"), // New
                            rs.getString("address")); // New
                    emp.setLeaveBalance(rs.getInt("leave_balance"));
                }
            }
            
            if (emp != null) {
                // Fetch Family Members List
                try (PreparedStatement ps = conn.prepareStatement(famSql)) {
                    ps.setString(1, id);
                    ResultSet rs = ps.executeQuery();
                    List<FamilyMember> members = new ArrayList<>();
                    while (rs.next()) {
                       members.add(new FamilyMember(
                           rs.getString("first_name"),
                           rs.getString("last_name"),
                           rs.getString("relation"),
                           rs.getString("email"),
                           rs.getString("phone")
                       )); 
                    }
                    emp.setFamilyMembers(members);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emp;
    }

    @Override
    public boolean registerEmployee(Employee emp) throws RemoteException {
        String insertUserSql = "INSERT INTO users(id, email, password, role) VALUES(?, ?, ?, ?)";
        String insertEmpSql = "INSERT INTO employees(id, first_name, last_name, ic_passport, designation, address, leave_balance) VALUES(?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Transaction

            // Insert into Users
            try (PreparedStatement pstmtUser = conn.prepareStatement(insertUserSql)) {
                pstmtUser.setString(1, emp.getId());
                pstmtUser.setString(2, emp.getEmail());
                pstmtUser.setString(3, emp.getPassword()); // Admin sets this now
                pstmtUser.setString(4, "EMP"); // Always EMP
                pstmtUser.executeUpdate();
            }

            // Insert into Employees
            try (PreparedStatement pstmtEmp = conn.prepareStatement(insertEmpSql)) {
                pstmtEmp.setString(1, emp.getId());
                pstmtEmp.setString(2, emp.getFirstName());
                pstmtEmp.setString(3, emp.getLastName());
                pstmtEmp.setString(4, emp.getIcPassport());
                pstmtEmp.setString(5, emp.getDesignation()); // New
                pstmtEmp.setString(6, emp.getAddress()); // New
                pstmtEmp.setInt(7, emp.getLeaveBalance());
                pstmtEmp.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
             if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            throw new RemoteException("Error registering employee: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    @Override
    public boolean updateProfile(Employee emp) throws RemoteException {
        // Update basic details
        String updateEmpSql = "UPDATE employees SET first_name=?, last_name=?, ic_passport=?, designation=?, address=? WHERE id=?";
        
        // Replace family members strategy: Delete all for this user, then insert new list
        String deleteFamSql = "DELETE FROM family_members WHERE employee_id=?";
        String insertFamSql = "INSERT INTO family_members(employee_id, first_name, last_name, relation, email, phone) VALUES(?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtEmp = conn.prepareStatement(updateEmpSql)) {
                 pstmtEmp.setString(1, emp.getFirstName());
                 pstmtEmp.setString(2, emp.getLastName());
                 pstmtEmp.setString(3, emp.getIcPassport());
                 pstmtEmp.setString(4, emp.getDesignation());
                 pstmtEmp.setString(5, emp.getAddress());
                 pstmtEmp.setString(6, emp.getId());
                 pstmtEmp.executeUpdate();
            }

            // Update family members
            try (PreparedStatement pstmtDel = conn.prepareStatement(deleteFamSql)) {
                pstmtDel.setString(1, emp.getId());
                pstmtDel.executeUpdate();
            }
            
            if (emp.getFamilyMembers() != null && !emp.getFamilyMembers().isEmpty()) {
                try (PreparedStatement pstmtIns = conn.prepareStatement(insertFamSql)) {
                    for (FamilyMember fm : emp.getFamilyMembers()) {
                        pstmtIns.setString(1, emp.getId());
                        pstmtIns.setString(2, fm.getFirstName());
                        pstmtIns.setString(3, fm.getLastName());
                        pstmtIns.setString(4, fm.getRelation());
                        pstmtIns.setString(5, fm.getEmail());
                        pstmtIns.setString(6, fm.getPhone());
                        pstmtIns.addBatch();
                    }
                    pstmtIns.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
             if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            throw new RemoteException("Error updating profile", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    
    // Deprecated method signature support if needed, redirecting to new one or throw
    public boolean updateProfile(Employee emp, common.FamilyDetail old) throws RemoteException {
        // Deprecated method signature support if needed, redirecting to new one or throw
        throw new RemoteException("Deprecated method called.");
    }

    @Override
    public int getLeaveBalance(String empId) throws RemoteException {
        String sql = "SELECT leave_balance FROM employees WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("leave_balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching leave balance", e);
        }
        return -1;
    }

    @Override
    public boolean applyForLeave(LeaveApplication leave) throws RemoteException {
        // Decrease balance -> Insert Leave
        // Logic same but deduction happens? Or do we deduct only on Approve?
        // Usually, in a real system, deduction happens on approval or provisional deduction on apply.
        // Current logic: Deduct immediately on apply.
        // Let's keep it consistent with previous logic for now.
        String updateBalanceSql = "UPDATE employees SET leave_balance = leave_balance - ? WHERE id = ? AND leave_balance >= ?";
        String insertLeaveSql = "INSERT INTO leave_applications(leave_id, employee_id, start_date, end_date, status, year) VALUES(?, ?, ?, ?, ?, ?)";

        long days = 1; 
        try {
             java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
             java.util.Date start = sdf.parse(leave.getStartDate());
             java.util.Date end = sdf.parse(leave.getEndDate());
             long diff = end.getTime() - start.getTime();
             days = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS) + 1;
        } catch (Exception e) { System.out.println("Date parse failed"); }

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtUpdate = conn.prepareStatement(updateBalanceSql)) {
                pstmtUpdate.setInt(1, (int) days);
                pstmtUpdate.setString(2, leave.getEmployeeID());
                pstmtUpdate.setInt(3, (int) days);
                if (pstmtUpdate.executeUpdate() == 0) {
                    conn.rollback(); return false;
                }
            }

            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertLeaveSql)) {
                pstmtInsert.setString(1, leave.getLeaveID());
                pstmtInsert.setString(2, leave.getEmployeeID());
                pstmtInsert.setString(3, leave.getStartDate());
                pstmtInsert.setString(4, leave.getEndDate());
                pstmtInsert.setString(5, leave.getStatus());
                pstmtInsert.setInt(6, leave.getYear());
                pstmtInsert.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
             if (conn != null) { try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } }
            e.printStackTrace();
            throw new RemoteException("Error applying for leave", e);
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); } }
        }
    }

    @Override
    public List<LeaveApplication> getLeaveHistory(String empId) throws RemoteException {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_applications WHERE employee_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LeaveApplication la = new LeaveApplication(
                    rs.getString("leave_id"),
                    rs.getString("employee_id"),
                    rs.getString("start_date"),
                    rs.getString("end_date"),
                    rs.getInt("year")
                );
                la.setStatus(rs.getString("status"));
                list.add(la);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error getting history", e);
        }
        return list;
    }

    @Override
    public String generateYearlyReport(String empId, int year) throws RemoteException {
        StringBuilder report = new StringBuilder();
        
        // Fetch User and Employee Details
        String empSql = "SELECT * FROM employees WHERE id = ?";
        String famSql = "SELECT * FROM family_members WHERE employee_id = ?";
        String leaveSql = "SELECT * FROM leave_applications WHERE employee_id = ? AND year = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            
            // Employee Info
            try (PreparedStatement ps = conn.prepareStatement(empSql)) {
                ps.setString(1, empId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    report.append("--- EMPLOYEE PROFILE ---\n");
                    report.append("ID: ").append(rs.getString("id")).append("\n");
                    report.append("Name: ").append(rs.getString("first_name")).append(" ").append(rs.getString("last_name")).append("\n");
                    report.append("Designation: ").append(rs.getString("designation")).append("\n");
                    report.append("Address: ").append(rs.getString("address")).append("\n");
                    report.append("Balance: ").append(rs.getInt("leave_balance")).append("\n");
                } else {
                    return "Employee not found.";
                }
            }

            // Family Info
            try (PreparedStatement ps = conn.prepareStatement(famSql)) {
                ps.setString(1, empId);
                ResultSet rs = ps.executeQuery();
                report.append("\n--- FAMILY DETAILS ---\n");
                while (rs.next()) {
                    report.append(rs.getString("relation") + ": " + 
                                  rs.getString("first_name") + " " + rs.getString("last_name") + 
                                  " (" + rs.getString("email") + ")\n");
                }
            }

            // Leave History
            try (PreparedStatement ps = conn.prepareStatement(leaveSql)) {
                ps.setString(1, empId);
                ps.setInt(2, year);
                ResultSet rs = ps.executeQuery();
                report.append("\n--- LEAVE HISTORY (" + year + ") ---\n");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    report.append(rs.getString("start_date")).append(" to ")
                          .append(rs.getString("end_date")).append(" [")
                          .append(rs.getString("status")).append("]\n");
                }
                if (!found) report.append("No leaves taken this year.\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error generating report", e);
        }
        
        return report.toString();
    }
    
    @Override
    public boolean syncWithPayrollSystem(String empId, int unpaidLeaves) throws RemoteException {
        // "Create a mock server method ... implements standard AES encryption to encrypt the payload before 'sending/logging' it"
        
        // Construct payload
        String payload = "SYNC_PAYROLL::EMP_ID=" + empId + "::UNPAID_LEAVES=" + unpaidLeaves + "::TIMESTAMP=" + System.currentTimeMillis();
        
        System.out.println("[Server] Preparing to sync with External Payroll System...");
        System.out.println("[Server] Original Payload: " + payload);
        
        // Encrypt
        String encryptedPayload = AESUtil.encrypt(payload);
        System.out.println("[Server] Encrypted Payload (Sending): " + encryptedPayload);
        
        // Simulate sending (logging here)
        System.out.println("[Server] Sync Successful >> Data sent to Payroll System API.");
        
        return true;
    }

    // -- New Admin Methods --

    @Override
    public List<Employee> getAllEmployees() throws RemoteException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employees.*, users.email, users.password, users.role FROM employees JOIN users ON employees.id = users.id";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Employee e = new Employee(
                    rs.getString("id"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("ic_passport"),
                    rs.getString("designation"),
                    rs.getString("address")
                );
                e.setLeaveBalance(rs.getInt("leave_balance"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error fetching employees", e);
        }
        return list;
    }

    @Override
    public boolean updateLeaveStatus(String leaveId, String status) throws RemoteException {
        String sql = "UPDATE leave_applications SET status = ? WHERE leave_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, leaveId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error updating leave status", e);
        }
    }

    @Override
    public boolean updateLeaveBalance(String empId, int newBalance) throws RemoteException {
        String sql = "UPDATE employees SET leave_balance = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newBalance);
            pstmt.setString(2, empId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("Error updating leave balance", e);
        }
    }

    @Override
    public List<LeaveApplication> getAllPendingLeaves() throws RemoteException {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_applications WHERE status = 'Pending'"; // Fixed string literal
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
             while (rs.next()) {
                 list.add(new LeaveApplication(
                     rs.getString("leave_id"),
                     rs.getString("employee_id"),
                     rs.getString("start_date"),
                     rs.getString("end_date"),
                     rs.getInt("year")
                 ));
             }
        } catch (SQLException e) {
             e.printStackTrace();
             throw new RemoteException("Error fetching pending leaves", e);
        }
        return list;
    }
}
