package common;

import java.util.ArrayList;
import java.util.List;

public class Employee extends User {
    private static final long serialVersionUID = 1L;
    
    private String firstName;
    private String lastName;
    private String icPassport;
    private int leaveBalance;
    private String designation; // New
    private String address; // New
    
    private List<FamilyMember> familyMembers = new ArrayList<>();

    public Employee(String id, String email, String password, String role, 
                    String firstName, String lastName, String icPassport,
                    String designation, String address) {
        super(id, email, password, role);
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.designation = designation;
        this.address = address;
        this.leaveBalance = 20;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getIcPassport() { return icPassport; }
    public void setIcPassport(String icPassport) { this.icPassport = icPassport; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getLeaveBalance() { return leaveBalance; }
    public void setLeaveBalance(int leaveBalance) { this.leaveBalance = leaveBalance; }

    public List<FamilyMember> getFamilyMembers() { return familyMembers; }
    public void setFamilyMembers(List<FamilyMember> familyMembers) { this.familyMembers = familyMembers; }
    
    public void addFamilyMember(FamilyMember member) {
        this.familyMembers.add(member);
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + designation + ")";
    }
}
