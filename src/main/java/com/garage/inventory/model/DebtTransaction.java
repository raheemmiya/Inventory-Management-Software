
package com.garage.inventory.model;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * DebtTransaction model class represents a debt/credit transaction
 * Tracks credit sales, payments, and adjustments for customers
 */
public class DebtTransaction {
    private int id;
    private int customerId;
    private Integer saleId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal remainingBalance;
    private Date transactionDate;
    private Date dueDate;
    private String paymentMethod;
    private String referenceNumber;
    private String notes;
    private DebtStatus status;

    public enum TransactionType {
        CREDIT_SALE, PAYMENT, ADJUSTMENT
    }

    public enum DebtStatus {
        PENDING, PARTIAL, PAID, OVERDUE
    }

    public DebtTransaction() {
    }

    public DebtTransaction(int customerId, Integer saleId, TransactionType transactionType,
                           BigDecimal amount, BigDecimal remainingBalance, Date transactionDate,
                           Date dueDate, String paymentMethod, String referenceNumber,
                           String notes, DebtStatus status) {
        this.customerId = customerId;
        this.saleId = saleId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.remainingBalance = remainingBalance;
        this.transactionDate = transactionDate;
        this.dueDate = dueDate;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
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

    public DebtStatus getStatus() {
        return status;
    }

    public void setStatus(DebtStatus status) {
        this.status = status;
    }

    public boolean isOverdue() {
        if (dueDate == null || status == DebtStatus.PAID) {
            return false;
        }
        Date today = new Date(System.currentTimeMillis());
        return dueDate.before(today) && status != DebtStatus.PAID;
    }
}