package com.garage.inventory.dao;

import com.garage.inventory.database.DatabaseConnection;
import com.garage.inventory.model.Item;
import com.garage.inventory.model.Sale;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SaleDAO handles all database operations for Sales
 * Manages sale transactions and automatically decreases inventory stock
 */
public class SaleDAO {

    /**
     * Adds a new sale transaction
     * This method records a sale and automatically decreases the item's stock quantity
     * Business logic: When items are sold, inventory stock must decrease
     *
     * @param sale The sale object containing sale details
     * @return true if sale was recorded successfully, false otherwise
     */
    public boolean addSale(Sale sale) {
        String saleSql = "INSERT INTO sales (item_id, customer_id, quantity, unit_price, total_amount, " +
                "sale_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        String checkStockSql = "SELECT stock_quantity FROM items WHERE id = ?";
        String updateStockSql = "UPDATE items SET stock_quantity = stock_quantity - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Check if enough stock is available
            int currentStock = 0;
            try (PreparedStatement stmt = conn.prepareStatement(checkStockSql)) {
                stmt.setInt(1, sale.getItemId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentStock = rs.getInt("stock_quantity");
                    } else {
                        conn.rollback();
                        System.err.println("Item not found: " + sale.getItemId());
                        return false; // Item doesn't exist
                    }
                }
            }

            if (currentStock < sale.getQuantity()) {
                conn.rollback();
                System.err.println("Insufficient stock. Available: " + currentStock + ", Required: " + sale.getQuantity());
                return false; // Insufficient stock
            }

            // Insert sale record
            try (PreparedStatement stmt = conn.prepareStatement(saleSql)) {
                stmt.setInt(1, sale.getItemId());
                stmt.setObject(2, sale.getCustomerId() > 0 ? sale.getCustomerId() : null);
                stmt.setInt(3, sale.getQuantity());
                stmt.setBigDecimal(4, sale.getUnitPrice());
                stmt.setBigDecimal(5, sale.getTotalAmount());
                stmt.setDate(6, sale.getSaleDate());
                stmt.setString(7, sale.getNotes());

                stmt.executeUpdate();
            }

            // Update stock quantity (decrease by sale quantity)
            // Using the same connection to maintain transaction integrity
            try (PreparedStatement stmt = conn.prepareStatement(updateStockSql)) {
                stmt.setInt(1, sale.getQuantity());
                stmt.setInt(2, sale.getItemId());

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
            System.err.println("Error adding sale: " + e.getMessage());
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
     * Retrieves all sale transactions
     *
     * @return List of all sales
     */
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC, id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Sale sale = new Sale();
                sale.setId(rs.getInt("id"));
                sale.setItemId(rs.getInt("item_id"));

                int customerId = rs.getInt("customer_id");
                sale.setCustomerId(rs.wasNull() ? 0 : customerId);

                sale.setQuantity(rs.getInt("quantity"));
                sale.setUnitPrice(rs.getBigDecimal("unit_price"));
                sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                sale.setSaleDate(rs.getDate("sale_date"));
                sale.setNotes(rs.getString("notes"));
                sales.add(sale);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sales: " + e.getMessage());
            e.printStackTrace();
        }

        return sales;
    }

    /**
     * Gets total sales for today
     * This is used for dashboard statistics
     *
     * @return Total amount of sales made today
     */
    public BigDecimal getTodaySalesTotal() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM sales WHERE sale_date = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's sales total: " + e.getMessage());
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    /**
     * Gets sales within a date range
     * Used for daily and monthly reports
     *
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of sales within the date range
     */
    public List<Sale> getSalesByDateRange(Date startDate, Date endDate) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("id"));
                    sale.setItemId(rs.getInt("item_id"));

                    int customerId = rs.getInt("customer_id");
                    sale.setCustomerId(rs.wasNull() ? 0 : customerId);

                    sale.setQuantity(rs.getInt("quantity"));
                    sale.setUnitPrice(rs.getBigDecimal("unit_price"));
                    sale.setTotalAmount(rs.getBigDecimal("total_amount"));
                    sale.setSaleDate(rs.getDate("sale_date"));
                    sale.setNotes(rs.getString("notes"));
                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sales by date range: " + e.getMessage());
            e.printStackTrace();
        }

        return sales;
    }
}