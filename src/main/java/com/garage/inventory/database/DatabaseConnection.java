package com.garage.inventory.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection class manages the connection to MySQL database
 * This class provides a singleton connection instance to ensure efficient database usage
 */
public class DatabaseConnection {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/garage_inventory";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root";
    
    private static Connection connection = null;
    
    /**
     * Private constructor to prevent instantiation (Singleton pattern)
     */
    private DatabaseConnection() {
    }
    
    /**
     * Gets a database connection instance
     * If connection doesn't exist or is closed, creates a new one
     * 
     * @return Connection object to the database
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Create connection to database
                connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
                System.out.println("Database connection established successfully!");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found!");
                throw new SQLException("MySQL JDBC Driver not found!", e);
            }
        }
        return connection;
    }
    
    /**
     * Closes the database connection
     * Should be called when application exits
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

