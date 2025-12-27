
package com.garage.inventory.model;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * DebtPayment model class represents a payment made against a debt
 * Tracks individual payments made by customers to reduce their outstanding balance
 */
public class DebtPayment {
    private int id;
    private int debtTransactionId;
    private BigDecimal paymentAmount;
    private Date paymentDate;
    private String paymentMethod;
    private String referenceNumber;
    private String notes;
    private String createdBy;

    public DebtPayment() {
    }

    public DebtPayment(int debtTransactionId, BigDecimal paymentAmount, Date paymentDate,
                       String paymentMethod, String referenceNumber, String notes, String createdBy) {
        this.debtTransactionId = debtTransactionId;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDebtTransactionId() {
        return debtTransactionId;
    }

    public void setDebtTransactionId(int debtTransactionId) {
        this.debtTransactionId = debtTransactionId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}