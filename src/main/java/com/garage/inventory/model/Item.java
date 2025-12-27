package com.garage.inventory.model;

import java.math.BigDecimal;

/**
 * Item model class represents a spare part/item in the inventory
 * Contains all information about an item including stock levels and pricing
 */
public class Item {
    private int id;
    private String partNumber;
    private String name;
    private String description;
    private String category;
    private BigDecimal unitPrice;
    private int stockQuantity;
    private int minStockLevel;
    private String location;
    private int supplierId;
    
    public Item() {
    }
    
    public Item(String partNumber, String name, String description, String category,
                BigDecimal unitPrice, int stockQuantity, int minStockLevel, 
                String location, int supplierId) {
        this.partNumber = partNumber;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.minStockLevel = minStockLevel;
        this.location = location;
        this.supplierId = supplierId;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getPartNumber() {
        return partNumber;
    }
    
    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public int getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public int getMinStockLevel() {
        return minStockLevel;
    }
    
    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public int getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }
    
    /**
     * Checks if the item is running low on stock
     * @return true if stock quantity is at or below minimum stock level
     */
    public boolean isLowStock() {
        return stockQuantity <= minStockLevel;
    }
    
    /**
     * Returns string representation for display in combo boxes
     * Shows item name and part number
     */
    @Override
    public String toString() {
        return name + " (" + partNumber + ")";
    }
}

