package com.garage.inventory.gui;

import com.garage.inventory.dao.CustomerDAO;
import com.garage.inventory.dao.SaleDAO;
import com.garage.inventory.model.Customer;
import com.garage.inventory.model.Sale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * CustomersScreen allows managing customers
 * Features: Add, Edit, Delete customers, View customer details and purchase history
 */
public class CustomersScreen extends JPanel {
    private MainApplication mainApp;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, contactNumberField, emailField, addressField, vehicleInfoField;
    private JTextField searchField;
    private CustomerDAO customerDAO;
    private SaleDAO saleDAO;
    private Customer selectedCustomer;
    private JTable salesTable;
    private DefaultTableModel salesTableModel;
    
    public CustomersScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        customerDAO = new CustomerDAO();
        saleDAO = new SaleDAO();
        
        initializeComponents();
        setupLayout();
        refreshData();
    }
    
    /**
     * Initializes all GUI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Initialize text fields
        nameField = new JTextField(20);
        contactNumberField = new JTextField(20);
        emailField = new JTextField(20);
        addressField = new JTextField(20);
        vehicleInfoField = new JTextField(20);
        searchField = new JTextField(20);
        
        // Initialize customer table
        String[] columns = {"ID", "Name", "Contact Number", "Email", "Address", "Vehicle Info"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectCustomerFromTable();
            }
        });
        
        // Initialize sales history table
        String[] salesColumns = {"Sale ID", "Item", "Quantity", "Unit Price", "Total Amount", "Date"};
        salesTableModel = new DefaultTableModel(salesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable = new JTable(salesTableModel);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    /**
     * Sets up the layout with form, table, and sales history
     */
    private void setupLayout() {
        // Top panel with search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Customers"));
        searchPanel.add(new JLabel("Search (Name/Contact):"));
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCustomers());
        searchPanel.add(searchButton);
        JButton clearSearchButton = new JButton("Clear");
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            refreshData();
        });
        searchPanel.add(clearSearchButton);
        
        // Form panel
        JPanel formPanel = createFormPanel();
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Left panel (form + buttons)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Right panel with customer table and sales history
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // Customer table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Customers List"));
        JScrollPane scrollPane = new JScrollPane(customerTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Sales history panel
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.setBorder(BorderFactory.createTitledBorder("Customer Purchase History"));
        salesPanel.setPreferredSize(new Dimension(0, 200));
        JScrollPane salesScrollPane = new JScrollPane(salesTable);
        salesPanel.add(salesScrollPane, BorderLayout.CENTER);
        
        // Combine right panel components
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, salesPanel);
        rightSplit.setDividerLocation(300);
        rightSplit.setResizeWeight(0.6);
        rightPanel.add(rightSplit, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        JPanel refreshPanel = new JPanel();
        refreshPanel.add(refreshButton);
        rightPanel.add(refreshPanel, BorderLayout.SOUTH);
        
        // Main split pane
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplit.setDividerLocation(400);
        mainSplit.setResizeWeight(0.3);
        
        add(searchPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
    }
    
    /**
     * Creates the form panel with all input fields
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Name
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Name *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);
        
        // Contact Number
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Contact Number:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(contactNumberField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(emailField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(addressField, gbc);
        
        // Vehicle Info
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Vehicle Info:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(vehicleInfoField, gbc);
        
        return panel;
    }
    
    /**
     * Creates the button panel with action buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Add New");
        addButton.addActionListener(e -> addCustomer());
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateCustomer());
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteCustomer());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Refreshes the data - loads all customers
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer customer : customers) {
            tableModel.addRow(new Object[]{
                customer.getId(),
                customer.getName(),
                customer.getContactNumber() != null ? customer.getContactNumber() : "N/A",
                customer.getEmail() != null ? customer.getEmail() : "N/A",
                customer.getAddress() != null ? customer.getAddress() : "N/A",
                customer.getVehicleInfo() != null ? customer.getVehicleInfo() : "N/A"
            });
        }
        updateSalesHistory();
    }
    
    /**
     * Searches customers by name or contact number
     */
    private void searchCustomers() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            refreshData();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Customer> customers = customerDAO.searchCustomers(searchTerm);
        for (Customer customer : customers) {
            tableModel.addRow(new Object[]{
                customer.getId(),
                customer.getName(),
                customer.getContactNumber() != null ? customer.getContactNumber() : "N/A",
                customer.getEmail() != null ? customer.getEmail() : "N/A",
                customer.getAddress() != null ? customer.getAddress() : "N/A",
                customer.getVehicleInfo() != null ? customer.getVehicleInfo() : "N/A"
            });
        }
    }
    
    /**
     * Selects a customer from the table and populates the form
     */
    private void selectCustomerFromTable() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            int customerId = (Integer) tableModel.getValueAt(selectedRow, 0);
            selectedCustomer = customerDAO.getCustomerById(customerId);
            
            if (selectedCustomer != null) {
                nameField.setText(selectedCustomer.getName());
                contactNumberField.setText(selectedCustomer.getContactNumber() != null ? selectedCustomer.getContactNumber() : "");
                emailField.setText(selectedCustomer.getEmail() != null ? selectedCustomer.getEmail() : "");
                addressField.setText(selectedCustomer.getAddress() != null ? selectedCustomer.getAddress() : "");
                vehicleInfoField.setText(selectedCustomer.getVehicleInfo() != null ? selectedCustomer.getVehicleInfo() : "");
                
                updateSalesHistory();
            }
        } else {
            salesTableModel.setRowCount(0);
        }
    }
    
    /**
     * Updates the sales history table for the selected customer
     */
    private void updateSalesHistory() {
        salesTableModel.setRowCount(0);
        if (selectedCustomer == null) {
            return;
        }
        
        List<Sale> allSales = saleDAO.getAllSales();
        BigDecimal totalSpent = BigDecimal.ZERO;
        
        for (Sale sale : allSales) {
            if (sale.getCustomerId() == selectedCustomer.getId()) {
                com.garage.inventory.model.Item item = new com.garage.inventory.dao.ItemDAO().getItemById(sale.getItemId());
                totalSpent = totalSpent.add(sale.getTotalAmount());
                
                salesTableModel.addRow(new Object[]{
                    sale.getId(),
                    item != null ? item.getName() : "N/A",
                    sale.getQuantity(),
                    "$" + String.format("%.2f", sale.getUnitPrice()),
                    "$" + String.format("%.2f", sale.getTotalAmount()),
                    sale.getSaleDate()
                });
            }
        }
    }
    
    /**
     * Adds a new customer
     */
    private void addCustomer() {
        if (!validateForm()) return;
        
        Customer customer = createCustomerFromForm();
        if (customer == null) return;
        
        if (customerDAO.addCustomer(customer)) {
            JOptionPane.showMessageDialog(this, "Customer added successfully!");
            clearForm();
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add customer.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Updates an existing customer
     */
    private void updateCustomer() {
        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!validateForm()) return;
        
        Customer customer = createCustomerFromForm();
        if (customer == null) return;
        
        customer.setId(selectedCustomer.getId());
        
        if (customerDAO.updateCustomer(customer)) {
            JOptionPane.showMessageDialog(this, "Customer updated successfully!");
            clearForm();
            selectedCustomer = null;
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update customer.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Deletes the selected customer
     */
    private void deleteCustomer() {
        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this customer?\n" + selectedCustomer.getName(),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (customerDAO.deleteCustomer(selectedCustomer.getId())) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                clearForm();
                selectedCustomer = null;
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Clears the form fields
     */
    private void clearForm() {
        nameField.setText("");
        contactNumberField.setText("");
        emailField.setText("");
        addressField.setText("");
        vehicleInfoField.setText("");
        selectedCustomer = null;
        customerTable.clearSelection();
        salesTableModel.setRowCount(0);
    }
    
    /**
     * Validates the form inputs
     */
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Creates a Customer object from form fields
     */
    private Customer createCustomerFromForm() {
        try {
            String name = nameField.getText().trim();
            String contactNumber = contactNumberField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String vehicleInfo = vehicleInfoField.getText().trim();
            
            Customer customer = new Customer(
                name,
                contactNumber.isEmpty() ? null : contactNumber,
                email.isEmpty() ? null : email,
                address.isEmpty() ? null : address,
                vehicleInfo.isEmpty() ? null : vehicleInfo
            );
            
            return customer;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating customer: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}

