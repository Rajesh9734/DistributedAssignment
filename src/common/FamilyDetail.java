package common;

import java.io.Serializable;

public class FamilyDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String spouseName;
    private int childrenCount;
    private String emergencyContact;

    public FamilyDetail(String spouseName, int childrenCount, String emergencyContact) {
        this.spouseName = spouseName;
        this.childrenCount = childrenCount;
        this.emergencyContact = emergencyContact;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public String toString() {
        return "Spouse: " + spouseName + ", Children: " + childrenCount + ", Emergency: " + emergencyContact;
    }
}

