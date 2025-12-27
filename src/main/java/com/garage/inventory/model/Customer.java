package com.garage.inventory.model;

/**
 * Customer model class represents a customer/client
 * Contains contact information and details about customers who purchase spare parts
 */
public class Customer {
    private int id;
    private String name;
    private String contactNumber;
    private String email;
    private String address;
    private String vehicleInfo;
    
    public Customer() {
    }
    
    public Customer(String name, String contactNumber, String email, String address, String vehicleInfo) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
        this.vehicleInfo = vehicleInfo;
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
    
    public String getVehicleInfo() {
        return vehicleInfo;
    }
    
    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }
    
    /**
     * Returns string representation for display in combo boxes
     * Shows customer name and contact number
     */
    @Override
    public String toString() {
        if (name == null || name.isEmpty()) {
            return "(No Customer)";
        }
        if (contactNumber != null && !contactNumber.isEmpty()) {
            return name + " (" + contactNumber + ")";
        }
        return name;
    }
}

