package com.garage.inventory.dao;

import com.garage.inventory.database.DatabaseConnection;
import com.garage.inventory.model.Admin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AdminDAO handles all database operations for Admin
 * Provides authentication functionality for the single admin account
 */
public class AdminDAO {
    
    /**
     * Authenticates admin by checking username and password
     * This is the login validation method
     * 
     * @param username The admin username
     * @param password The admin password
     * @return Admin object if credentials are valid, null otherwise
     */
    public Admin authenticate(String username, String password) {
        String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setId(rs.getInt("id"));
                    admin.setUsername(rs.getString("username"));
                    admin.setPassword(rs.getString("password"));
                    return admin;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating admin: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}

