package com.garage.inventory;

import com.garage.inventory.gui.LoginScreen;
import javax.swing.SwingUtilities;

/**
 * Main entry point for the Garage Inventory Management System
 * This application starts with the login screen
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create and display the login screen
                LoginScreen loginScreen = new LoginScreen();
                loginScreen.setVisible(true);
            }
        });
    }
}

