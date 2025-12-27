package com.garage.inventory.dao;

import com.garage.inventory.database.DatabaseConnection;
import com.garage.inventory.model.Item;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO handles all database operations for Items/Spare Parts
 * Manages CRUD operations for inventory items
 */
public class ItemDAO {
    
    /**
     * Adds a new item to the inventory
     * This method inserts a new spare part into the database
     * 
     * @param item The item object containing all item details
     * @return true if item was added successfully, false otherwise
     */
    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (part_number, name, description, category, unit_price, " +
                     "stock_quantity, min_stock_level, location, supplier_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, item.getPartNumber());
            stmt.setString(2, item.getName());
            stmt.setString(3, item.getDescription());
            stmt.setString(4, item.getCategory());
            stmt.setBigDecimal(5, item.getUnitPrice());
            stmt.setInt(6, item.getStockQuantity());
            stmt.setInt(7, item.getMinStockLevel());
            stmt.setString(8, item.getLocation());
            stmt.setInt(9, item.getSupplierId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Updates an existing item's details
     * This method modifies item information in the database
     * 
     * @param item The item object with updated information
     * @return true if item was updated successfully, false otherwise
     */
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET part_number = ?, name = ?, description = ?, category = ?, " +
                     "unit_price = ?, stock_quantity = ?, min_stock_level = ?, location = ?, " +
                     "supplier_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, item.getPartNumber());
            stmt.setString(2, item.getName());
            stmt.setString(3, item.getDescription());
            stmt.setString(4, item.getCategory());
            stmt.setBigDecimal(5, item.getUnitPrice());
            stmt.setInt(6, item.getStockQuantity());
            stmt.setInt(7, item.getMinStockLevel());
            stmt.setString(8, item.getLocation());
            stmt.setInt(9, item.getSupplierId());
            stmt.setInt(10, item.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes an item from the inventory
     * This permanently removes the item from the database
     * 
     * @param itemId The ID of the item to delete
     * @return true if item was deleted successfully, false otherwise
     */
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves all items from the inventory
     * This method gets all spare parts for display in the inventory management screen
     * 
     * @return List of all items in the inventory
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("id"));
                item.setPartNumber(rs.getString("part_number"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setStockQuantity(rs.getInt("stock_quantity"));
                item.setMinStockLevel(rs.getInt("min_stock_level"));
                item.setLocation(rs.getString("location"));
                item.setSupplierId(rs.getInt("supplier_id"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Retrieves an item by its ID
     * 
     * @param itemId The ID of the item to retrieve
     * @return Item object if found, null otherwise
     */
    public Item getItemById(int itemId) {
        String sql = "SELECT * FROM items WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, itemId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Item item = new Item();
                    item.setId(rs.getInt("id"));
                    item.setPartNumber(rs.getString("part_number"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setCategory(rs.getString("category"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setStockQuantity(rs.getInt("stock_quantity"));
                    item.setMinStockLevel(rs.getInt("min_stock_level"));
                    item.setLocation(rs.getString("location"));
                    item.setSupplierId(rs.getInt("supplier_id"));
                    return item;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving item: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Updates stock quantity when items are purchased or sold
     * This method is called automatically when purchases or sales are made
     * 
     * @param itemId The ID of the item
     * @param quantityChange The change in quantity (positive for purchase, negative for sale)
     * @return true if stock was updated successfully, false otherwise
     */
    public boolean updateStockQuantity(int itemId, int quantityChange) {
        String sql = "UPDATE items SET stock_quantity = stock_quantity + ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, itemId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating stock quantity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves items that are running low on stock
     * Items are considered low stock when quantity <= min_stock_level
     * This is used for low stock alerts on the dashboard
     * 
     * @return List of items with low stock
     */
    public List<Item> getLowStockItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE stock_quantity <= min_stock_level ORDER BY stock_quantity";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("id"));
                item.setPartNumber(rs.getString("part_number"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setUnitPrice(rs.getBigDecimal("unit_price"));
                item.setStockQuantity(rs.getInt("stock_quantity"));
                item.setMinStockLevel(rs.getInt("min_stock_level"));
                item.setLocation(rs.getString("location"));
                item.setSupplierId(rs.getInt("supplier_id"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving low stock items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Gets the total count of items in stock
     * This counts distinct items, not total quantity
     * 
     * @return Total number of items in inventory
     */
    public int getTotalItemCount() {
        String sql = "SELECT COUNT(*) as total FROM items";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total item count: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Checks if a part number already exists
     * Used to prevent duplicate part numbers when adding items
     * 
     * @param partNumber The part number to check
     * @return true if part number exists, false otherwise
     */
    public boolean partNumberExists(String partNumber) {
        String sql = "SELECT COUNT(*) as count FROM items WHERE part_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, partNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking part number: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
}

