package com.garage.inventory.model;

/**
 * Supplier model class represents a supplier/vendor
 * Contains contact information and details about suppliers
 */
public class Supplier {
    private int id;
    private String name;
    private String contactNumber;
    private String email;
    private String address;
    
    public Supplier() {
    }
    
    public Supplier(String name, String contactNumber, String email, String address) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * Returns string representation for display in combo boxes
     * Shows supplier name
     */
    @Override
    public String toString() {
        if (name == null || name.isEmpty()) {
            return "(No Supplier)";
        }
        return name;
    }
}

