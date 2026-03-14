package common;

import java.io.Serializable;

public class LeaveApplication implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String leaveID;
    private String employeeID;
    private String startDate;
    private String endDate;
    private String status; // "Pending", "Approved", "Rejected"
    private int year;

    public LeaveApplication(String leaveID, String employeeID, String startDate, String endDate, int year) {
        this.leaveID = leaveID;
        this.employeeID = employeeID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "Pending"; // Default status
        this.year = year;
    }

    // Getters and Setters
    public String getLeaveID() {
        return leaveID;
    }

    public void setLeaveID(String leaveID) {
        this.leaveID = leaveID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "ID: " + leaveID + " | " + startDate + " to " + endDate + " | Status: " + status;
    }
}

