package com.garage.inventory.dao;

import com.garage.inventory.database.DatabaseConnection;
import com.garage.inventory.model.Supplier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SupplierDAO handles all database operations for Suppliers
 * Manages supplier information and relationships with items
 */
public class SupplierDAO {
    
    /**
     * Adds a new supplier to the database
     * This method inserts supplier information for tracking vendors
     * 
     * @param supplier The supplier object containing all supplier details
     * @return true if supplier was added successfully, false otherwise
     */
    public boolean addSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact_number, email, address) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactNumber());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getAddress());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Updates an existing supplier's information
     * This method modifies supplier details in the database
     * 
     * @param supplier The supplier object with updated information
     * @return true if supplier was updated successfully, false otherwise
     */
    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact_number = ?, email = ?, address = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, supplier.getName());
            stmt.setString(2, supplier.getContactNumber());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getAddress());
            stmt.setInt(5, supplier.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a supplier from the database
     * Note: This sets supplier_id to NULL in items table (due to ON DELETE SET NULL constraint)
     * 
     * @param supplierId The ID of the supplier to delete
     * @return true if supplier was deleted successfully, false otherwise
     */
    public boolean deleteSupplier(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves all suppliers from the database
     * This method gets all suppliers for display in the suppliers management screen
     * 
     * @return List of all suppliers
     */
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setContactNumber(rs.getString("contact_number"));
                supplier.setEmail(rs.getString("email"));
                supplier.setAddress(rs.getString("address"));
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving suppliers: " + e.getMessage());
            e.printStackTrace();
        }
        
        return suppliers;
    }
    
    /**
     * Retrieves a supplier by its ID
     * 
     * @param supplierId The ID of the supplier to retrieve
     * @return Supplier object if found, null otherwise
     */
    public Supplier getSupplierById(int supplierId) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Supplier supplier = new Supplier();
                    supplier.setId(rs.getInt("id"));
                    supplier.setName(rs.getString("name"));
                    supplier.setContactNumber(rs.getString("contact_number"));
                    supplier.setEmail(rs.getString("email"));
                    supplier.setAddress(rs.getString("address"));
                    return supplier;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving supplier: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets items supplied by a specific supplier
     * This shows which parts are provided by each supplier
     * 
     * @param supplierId The ID of the supplier
     * @return List of item IDs supplied by this supplier
     */
    public List<Integer> getItemsBySupplier(int supplierId) {
        List<Integer> itemIds = new ArrayList<>();
        String sql = "SELECT item_id FROM supplier_items WHERE supplier_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itemIds.add(rs.getInt("item_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving supplier items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return itemIds;
    }
    
    /**
     * Links an item to a supplier
     * This establishes the relationship that a supplier provides a specific part
     * 
     * @param supplierId The ID of the supplier
     * @param itemId The ID of the item
     * @return true if link was created successfully, false otherwise
     */
    public boolean linkItemToSupplier(int supplierId, int itemId) {
        String sql = "INSERT INTO supplier_items (supplier_id, item_id) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE supplier_id = supplier_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, supplierId);
            stmt.setInt(2, itemId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error linking item to supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

