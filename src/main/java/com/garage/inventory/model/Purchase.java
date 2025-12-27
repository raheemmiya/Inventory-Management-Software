package com.garage.inventory.model;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Purchase model class represents a purchase transaction
 * Records when items are purchased from suppliers and updates inventory
 */
public class Purchase {
    private int id;
    private int itemId;
    private int supplierId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Date purchaseDate;
    private String invoiceNumber;
    private String notes;
    
    public Purchase() {
    }
    
    public Purchase(int itemId, int supplierId, int quantity, BigDecimal unitPrice,
                   BigDecimal totalAmount, Date purchaseDate, String invoiceNumber, String notes) {
        this.itemId = itemId;
        this.supplierId = supplierId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.purchaseDate = purchaseDate;
        this.invoiceNumber = invoiceNumber;
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public int getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Date getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

