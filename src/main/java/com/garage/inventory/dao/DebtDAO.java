package com.garage.inventory.dao;

import com.garage.inventory.database.DatabaseConnection;
import com.garage.inventory.model.DebtTransaction;
import com.garage.inventory.model.DebtPayment;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebtDAO {

    public boolean addDebtTransaction(DebtTransaction debt) {
        String sql = "INSERT INTO debt_transactions (customer_id, sale_id, transaction_type, amount, " +
                "remaining_balance, transaction_date, due_date, payment_method, reference_number, " +
                "notes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, debt.getCustomerId());
            if (debt.getSaleId() != null) {
                stmt.setInt(2, debt.getSaleId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, debt.getTransactionType().name());
            stmt.setBigDecimal(4, debt.getAmount());
            stmt.setBigDecimal(5, debt.getRemainingBalance());
            stmt.setDate(6, debt.getTransactionDate());
            if (debt.getDueDate() != null) {
                stmt.setDate(7, debt.getDueDate());
            } else {
                stmt.setNull(7, Types.DATE);
            }
            stmt.setString(8, debt.getPaymentMethod());
            stmt.setString(9, debt.getReferenceNumber());
            stmt.setString(10, debt.getNotes());
            stmt.setString(11, debt.getStatus().name());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding debt transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean recordPayment(DebtPayment payment) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String paymentSql = "INSERT INTO debt_payments (debt_transaction_id, payment_amount, " +
                    "payment_date, payment_method, reference_number, notes, created_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(paymentSql)) {
                stmt.setInt(1, payment.getDebtTransactionId());
                stmt.setBigDecimal(2, payment.getPaymentAmount());
                stmt.setDate(3, payment.getPaymentDate());
                stmt.setString(4, payment.getPaymentMethod());
                stmt.setString(5, payment.getReferenceNumber());
                stmt.setString(6, payment.getNotes());
                stmt.setString(7, payment.getCreatedBy());
                stmt.executeUpdate();
            }

            String updateSql = "UPDATE debt_transactions SET remaining_balance = remaining_balance - ?, " +
                    "status = CASE " +
                    "WHEN remaining_balance - ? <= 0 THEN 'PAID' " +
                    "WHEN remaining_balance - ? < amount THEN 'PARTIAL' " +
                    "ELSE status END " +
                    "WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setBigDecimal(1, payment.getPaymentAmount());
                stmt.setBigDecimal(2, payment.getPaymentAmount());
                stmt.setBigDecimal(3, payment.getPaymentAmount());
                stmt.setInt(4, payment.getDebtTransactionId());
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error recording payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<DebtTransaction> getAllDebtTransactions() {
        List<DebtTransaction> debts = new ArrayList<>();
        String sql = "SELECT * FROM debt_transactions ORDER BY transaction_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                debts.add(mapResultSetToDebt(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving debt transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return debts;
    }

    public List<DebtTransaction> getDebtsByCustomer(int customerId) {
        List<DebtTransaction> debts = new ArrayList<>();
        String sql = "SELECT * FROM debt_transactions WHERE customer_id = ? ORDER BY transaction_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    debts.add(mapResultSetToDebt(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving customer debts: " + e.getMessage());
            e.printStackTrace();
        }

        return debts;
    }

    public List<DebtTransaction> getPendingDebts() {
        List<DebtTransaction> debts = new ArrayList<>();
        String sql = "SELECT * FROM debt_transactions WHERE status IN ('PENDING', 'PARTIAL', 'OVERDUE') " +
                "ORDER BY transaction_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                debts.add(mapResultSetToDebt(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pending debts: " + e.getMessage());
            e.printStackTrace();
        }

        return debts;
    }

    public List<DebtTransaction> getOverdueDebts() {
        List<DebtTransaction> debts = new ArrayList<>();
        String sql = "SELECT * FROM debt_transactions WHERE status = 'OVERDUE' OR " +
                "(due_date < CURDATE() AND status IN ('PENDING', 'PARTIAL')) " +
                "ORDER BY due_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                debts.add(mapResultSetToDebt(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving overdue debts: " + e.getMessage());
            e.printStackTrace();
        }

        return debts;
    }

    public List<DebtPayment> getPaymentHistory(int debtTransactionId) {
        List<DebtPayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM debt_payments WHERE debt_transaction_id = ? ORDER BY payment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, debtTransactionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving payment history: " + e.getMessage());
            e.printStackTrace();
        }

        return payments;
    }

    public BigDecimal getTotalOutstandingDebt() {
        String sql = "SELECT SUM(remaining_balance) as total FROM debt_transactions " +
                "WHERE status IN ('PENDING', 'PARTIAL', 'OVERDUE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("Error getting total outstanding debt: " + e.getMessage());
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    public Map<String, Object> getCustomerDebtSummary(int customerId) {
        Map<String, Object> summary = new HashMap<>();
        String sql = "SELECT " +
                "SUM(CASE WHEN transaction_type = 'CREDIT_SALE' THEN amount ELSE 0 END) as total_credit, " +
                "SUM(CASE WHEN transaction_type = 'PAYMENT' THEN amount ELSE 0 END) as total_paid, " +
                "SUM(remaining_balance) as outstanding, " +
                "COUNT(CASE WHEN status = 'OVERDUE' THEN 1 END) as overdue_count " +
                "FROM debt_transactions WHERE customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.put("totalCredit", rs.getBigDecimal("total_credit"));
                    summary.put("totalPaid", rs.getBigDecimal("total_paid"));
                    summary.put("outstanding", rs.getBigDecimal("outstanding"));
                    summary.put("overdueCount", rs.getInt("overdue_count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting customer debt summary: " + e.getMessage());
            e.printStackTrace();
        }

        return summary;
    }

    public void updateOverdueDebts() {
        String sql = "UPDATE debt_transactions SET status = 'OVERDUE' " +
                "WHERE due_date < CURDATE() AND status IN ('PENDING', 'PARTIAL')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating overdue debts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean deleteDebtTransaction(int debtId) {
        String sql = "DELETE FROM debt_transactions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, debtId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting debt transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private DebtTransaction mapResultSetToDebt(ResultSet rs) throws SQLException {
        DebtTransaction debt = new DebtTransaction();
        debt.setId(rs.getInt("id"));
        debt.setCustomerId(rs.getInt("customer_id"));

        int saleId = rs.getInt("sale_id");
        debt.setSaleId(rs.wasNull() ? null : saleId);

        debt.setTransactionType(DebtTransaction.TransactionType.valueOf(rs.getString("transaction_type")));
        debt.setAmount(rs.getBigDecimal("amount"));
        debt.setRemainingBalance(rs.getBigDecimal("remaining_balance"));
        debt.setTransactionDate(rs.getDate("transaction_date"));
        debt.setDueDate(rs.getDate("due_date"));
        debt.setPaymentMethod(rs.getString("payment_method"));
        debt.setReferenceNumber(rs.getString("reference_number"));
        debt.setNotes(rs.getString("notes"));
        debt.setStatus(DebtTransaction.DebtStatus.valueOf(rs.getString("status")));

        return debt;
    }

    private DebtPayment mapResultSetToPayment(ResultSet rs) throws SQLException {
        DebtPayment payment = new DebtPayment();
        payment.setId(rs.getInt("id"));
        payment.setDebtTransactionId(rs.getInt("debt_transaction_id"));
        payment.setPaymentAmount(rs.getBigDecimal("payment_amount"));
        payment.setPaymentDate(rs.getDate("payment_date"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setReferenceNumber(rs.getString("reference_number"));
        payment.setNotes(rs.getString("notes"));
        payment.setCreatedBy(rs.getString("created_by"));

        return payment;
    }
}