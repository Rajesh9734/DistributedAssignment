package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface HRMInterface extends Remote {
    
    // User login(String email, String password) throws RemoteException;
    User login(String email, String password) throws RemoteException;
    
    // boolean registerEmployee(Employee emp) throws RemoteException;
    boolean registerEmployee(Employee emp) throws RemoteException;
    
    // Updates profile details and family list
    boolean updateProfile(Employee emp) throws RemoteException;
    
    // int getLeaveBalance(String empId) throws RemoteException;
    int getLeaveBalance(String empId) throws RemoteException;
    
    // boolean applyForLeave(LeaveApplication leave) throws RemoteException;
    boolean applyForLeave(LeaveApplication leave) throws RemoteException;
    
    // List<LeaveApplication> getLeaveHistory(String empId) throws RemoteException;
    List<LeaveApplication> getLeaveHistory(String empId) throws RemoteException;
    
    // String generateYearlyReport(String empId, int year) throws RemoteException;
    String generateYearlyReport(String empId, int year) throws RemoteException;
    
    // boolean syncWithPayrollSystem(String empId, int unpaidLeaves) throws RemoteException;
    boolean syncWithPayrollSystem(String empId, int unpaidLeaves) throws RemoteException;
    
    // -- New Admin Methods --
    // List<Employee> getAllEmployees() throws RemoteException;
    List<Employee> getAllEmployees() throws RemoteException;
    
    // boolean updateLeaveStatus(String leaveId, String status) throws RemoteException;
    boolean updateLeaveStatus(String leaveId, String status) throws RemoteException;
    
    // boolean updateLeaveBalance(String empId, int newBalance) throws RemoteException;
    boolean updateLeaveBalance(String empId, int newBalance) throws RemoteException;
    
    // Fetch specifically pending leaves? 
    // List<LeaveApplication> getAllPendingLeaves() throws RemoteException;
    List<LeaveApplication> getAllPendingLeaves() throws RemoteException;
}
