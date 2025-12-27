
package com.garage.inventory.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.garage.inventory.database.DatabaseConnection;

/**
 * MainApplication is the main window that contains all screens
 * Manages navigation between different screens using a card layout
 */
public class MainApplication extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private DashboardScreen dashboardScreen;
    private InventoryManagementScreen inventoryScreen;
    private PurchaseScreen purchaseScreen;
    private SalesScreen salesScreen;
    private ReportsScreen reportsScreen;
    private SuppliersScreen suppliersScreen;
    private CustomersScreen customersScreen;
    private DebtScreen debtScreen;

    public MainApplication() {
        initializeComponents();
        setupLayout();
        setupMenuBar();
        attachEventListeners();
    }

    /**
     * Initializes all components
     */
    private void initializeComponents() {
        setTitle("Garage Inventory Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize all screens
        dashboardScreen = new DashboardScreen(this);
        inventoryScreen = new InventoryManagementScreen(this);
        purchaseScreen = new PurchaseScreen(this);
        salesScreen = new SalesScreen(this);
        reportsScreen = new ReportsScreen(this);
        suppliersScreen = new SuppliersScreen(this);
        customersScreen = new CustomersScreen(this);
        debtScreen = new DebtScreen(this); // FIXED: Pass 'this' parameter
    }

    /**
     * Sets up the card layout with all screens
     */
    private void setupLayout() {
        // Add all screens to card layout
        mainPanel.add(dashboardScreen, "DASHBOARD");
        mainPanel.add(inventoryScreen, "INVENTORY");
        mainPanel.add(purchaseScreen, "PURCHASE");
        mainPanel.add(salesScreen, "SALES");
        mainPanel.add(reportsScreen, "REPORTS");
        mainPanel.add(suppliersScreen, "SUPPLIERS");
        mainPanel.add(customersScreen, "CUSTOMERS");
        mainPanel.add(debtScreen, "DEBT"); // ADDED: Debt screen

        add(mainPanel, BorderLayout.CENTER);

        // Show dashboard by default
        showScreen("DASHBOARD");
    }

    /**
     * Creates the menu bar for navigation
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Navigation Menu
        JMenu navMenu = new JMenu("Navigation");
        navMenu.setMnemonic('N');

        JMenuItem dashboardItem = new JMenuItem("Dashboard");
        dashboardItem.setMnemonic('D');
        dashboardItem.addActionListener(e -> showScreen("DASHBOARD"));

        JMenuItem inventoryItem = new JMenuItem("Inventory Management");
        inventoryItem.setMnemonic('I');
        inventoryItem.addActionListener(e -> showScreen("INVENTORY"));

        JMenuItem purchaseItem = new JMenuItem("Purchase");
        purchaseItem.setMnemonic('P');
        purchaseItem.addActionListener(e -> showScreen("PURCHASE"));

        JMenuItem salesItem = new JMenuItem("Sales");
        salesItem.setMnemonic('S');
        salesItem.addActionListener(e -> showScreen("SALES"));

        JMenuItem reportsItem = new JMenuItem("Reports");
        reportsItem.setMnemonic('R');
        reportsItem.addActionListener(e -> showScreen("REPORTS"));

        JMenuItem suppliersItem = new JMenuItem("Suppliers");
        suppliersItem.setMnemonic('u');
        suppliersItem.addActionListener(e -> showScreen("SUPPLIERS"));

        JMenuItem customersItem = new JMenuItem("Customers");
        customersItem.setMnemonic('C');
        customersItem.addActionListener(e -> showScreen("CUSTOMERS"));

        JMenuItem debtItem = new JMenuItem("Debt Management"); // ADDED
        debtItem.setMnemonic('b');
        debtItem.addActionListener(e -> showScreen("DEBT"));

        navMenu.add(dashboardItem);
        navMenu.addSeparator();
        navMenu.add(inventoryItem);
        navMenu.add(purchaseItem);
        navMenu.add(salesItem);
        navMenu.addSeparator();
        navMenu.add(reportsItem);
        navMenu.add(suppliersItem);
        navMenu.add(customersItem);
        navMenu.add(debtItem); // ADDED

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.setMnemonic('L');
        logoutItem.addActionListener(e -> logout());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        menuBar.add(navMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Attaches window event listeners
     */
    private void attachEventListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    /**
     * Shows a specific screen by name
     *
     * @param screenName The name of the screen to show
     */
    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);

        // Refresh dashboard when shown
        if (screenName.equals("DASHBOARD")) {
            dashboardScreen.refreshData();
        } else if (screenName.equals("INVENTORY")) {
            inventoryScreen.refreshData();
        } else if (screenName.equals("PURCHASE")) {
            purchaseScreen.refreshData();
        } else if (screenName.equals("SALES")) {
            salesScreen.refreshData();
        } else if (screenName.equals("REPORTS")) {
            reportsScreen.refreshData();
        } else if (screenName.equals("SUPPLIERS")) {
            suppliersScreen.refreshData();
        } else if (screenName.equals("CUSTOMERS")) {
            customersScreen.refreshData();
        } else if (screenName.equals("DEBT")) { // ADDED
            debtScreen.refreshData();
        }
    }

    /**
     * Logs out and returns to login screen
     */
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginScreen().setVisible(true);
        }
    }

    /**
     * Exits the application and closes database connection
     */
    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit Application",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseConnection.closeConnection();
            System.exit(0);
        }
    }
}
        