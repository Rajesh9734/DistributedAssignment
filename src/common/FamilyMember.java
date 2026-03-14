package common;

import java.io.Serializable;

public class FamilyMember implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String firstName;
    private String lastName;
    private String relation; // Father, Mother, Spouse, Child
    private String email;
    private String phone;
    
    public FamilyMember(String firstName, String lastName, String relation, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.relation = relation;
        this.email = email;
        this.phone = phone;
    }
    
    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    @Override
    public String toString() {
        return relation + ": " + firstName + " " + lastName + " (" + email + ")";
    }
}

