package com.garage.inventory.dao;

import com.garage.inventory.database.DatabaseConnection;
import com.garage.inventory.model.Purchase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * PurchaseDAO handles all database operations for Purchases
 * Manages purchase transactions and automatically updates inventory stock
 */
public class PurchaseDAO {

    private ItemDAO itemDAO = new ItemDAO();

    /**
     * Adds a new purchase transaction
     * This method records a purchase and automatically increases the item's stock quantity
     * Business logic: When items are purchased, inventory stock must increase
     *
     * @param purchase The purchase object containing purchase details
     * @return true if purchase was recorded successfully, false otherwise
     */
    public boolean addPurchase(Purchase purchase) {
        String purchaseSql = "INSERT INTO purchases (item_id, supplier_id, quantity, unit_price, " +
                "total_amount, purchase_date, invoice_number, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String updateStockSql = "UPDATE items SET stock_quantity = stock_quantity + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert purchase record
            try (PreparedStatement stmt = conn.prepareStatement(purchaseSql)) {
                stmt.setInt(1, purchase.getItemId());
                stmt.setObject(2, purchase.getSupplierId() > 0 ? purchase.getSupplierId() : null);
                stmt.setInt(3, purchase.getQuantity());
                stmt.setBigDecimal(4, purchase.getUnitPrice());
                stmt.setBigDecimal(5, purchase.getTotalAmount());
                stmt.setDate(6, purchase.getPurchaseDate());
                stmt.setString(7, purchase.getInvoiceNumber());
                stmt.setString(8, purchase.getNotes());

                stmt.executeUpdate();
            }

            // Update stock quantity (increase by purchase quantity)
            // Using the same connection to maintain transaction integrity
            try (PreparedStatement stmt = conn.prepareStatement(updateStockSql)) {
                stmt.setInt(1, purchase.getQuantity());
                stmt.setInt(2, purchase.getItemId());

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    conn.rollback();
                    System.err.println("Failed to update stock - item not found");
                    return false;
                }
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding purchase: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves all purchase transactions
     *
     * @return List of all purchases
     */
    public List<Purchase> getAllPurchases() {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases ORDER BY purchase_date DESC, id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Purchase purchase = new Purchase();
                purchase.setId(rs.getInt("id"));
                purchase.setItemId(rs.getInt("item_id"));

                int supplierId = rs.getInt("supplier_id");
                purchase.setSupplierId(rs.wasNull() ? 0 : supplierId);

                purchase.setQuantity(rs.getInt("quantity"));
                purchase.setUnitPrice(rs.getBigDecimal("unit_price"));
                purchase.setTotalAmount(rs.getBigDecimal("total_amount"));
                purchase.setPurchaseDate(rs.getDate("purchase_date"));
                purchase.setInvoiceNumber(rs.getString("invoice_number"));
                purchase.setNotes(rs.getString("notes"));
                purchases.add(purchase);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving purchases: " + e.getMessage());
            e.printStackTrace();
        }

        return purchases;
    }

    /**
     * Gets total purchases for today
     * This is used for dashboard statistics
     *
     * @return Total amount of purchases made today
     */
    public BigDecimal getTodayPurchasesTotal() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM purchases WHERE purchase_date = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's purchases total: " + e.getMessage());
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    /**
     * Gets purchases within a date range
     * Used for monthly reports
     *
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of purchases within the date range
     */
    public List<Purchase> getPurchasesByDateRange(Date startDate, Date endDate) {
        List<Purchase> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases WHERE purchase_date BETWEEN ? AND ? ORDER BY purchase_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Purchase purchase = new Purchase();
                    purchase.setId(rs.getInt("id"));
                    purchase.setItemId(rs.getInt("item_id"));

                    int supplierId = rs.getInt("supplier_id");
                    purchase.setSupplierId(rs.wasNull() ? 0 : supplierId);

                    purchase.setQuantity(rs.getInt("quantity"));
                    purchase.setUnitPrice(rs.getBigDecimal("unit_price"));
                    purchase.setTotalAmount(rs.getBigDecimal("total_amount"));
                    purchase.setPurchaseDate(rs.getDate("purchase_date"));
                    purchase.setInvoiceNumber(rs.getString("invoice_number"));
                    purchase.setNotes(rs.getString("notes"));
                    purchases.add(purchase);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving purchases by date range: " + e.getMessage());
            e.printStackTrace();
        }

        return purchases;
    }
}