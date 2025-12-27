package com.garage.inventory.gui;

import com.garage.inventory.dao.ItemDAO;
import com.garage.inventory.dao.SupplierDAO;
import com.garage.inventory.model.Item;
import com.garage.inventory.model.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * SuppliersScreen allows managing suppliers
 * Features: Add, Edit, Delete suppliers, View supplier details
 */
public class SuppliersScreen extends JPanel {
    private MainApplication mainApp;
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, contactNumberField, emailField, addressField;
    private SupplierDAO supplierDAO;
    private ItemDAO itemDAO;
    private Supplier selectedSupplier;
    private JTextArea itemsTextArea;
    
    public SuppliersScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        supplierDAO = new SupplierDAO();
        itemDAO = new ItemDAO();
        
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
        
        // Initialize table
        String[] columns = {"ID", "Name", "Contact Number", "Email", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        supplierTable = new JTable(tableModel);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectSupplierFromTable();
            }
        });
    }
    
    /**
     * Sets up the layout with form and table
     */
    private void setupLayout() {
        // Form panel
        JPanel formPanel = createFormPanel();
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Left panel (form + buttons)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Suppliers List"));
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Items supplied panel
        JPanel itemsPanel = createItemsSuppliedPanel();
        tablePanel.add(itemsPanel, BorderLayout.SOUTH);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        JPanel refreshPanel = new JPanel();
        refreshPanel.add(refreshButton);
        tablePanel.add(refreshPanel, BorderLayout.NORTH);
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, tablePanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.3);
        
        add(splitPane, BorderLayout.CENTER);
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
        
        return panel;
    }
    
    /**
     * Creates the button panel with action buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Add New");
        addButton.addActionListener(e -> addSupplier());
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateSupplier());
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSupplier());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Creates panel showing items supplied by selected supplier
     */
    private JPanel createItemsSuppliedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Parts Supplied"));
        panel.setPreferredSize(new Dimension(0, 150));
        
        itemsTextArea = new JTextArea();
        itemsTextArea.setEditable(false);
        itemsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(itemsTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Refreshes the data - loads all suppliers
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            tableModel.addRow(new Object[]{
                supplier.getId(),
                supplier.getName(),
                supplier.getContactNumber() != null ? supplier.getContactNumber() : "N/A",
                supplier.getEmail() != null ? supplier.getEmail() : "N/A",
                supplier.getAddress() != null ? supplier.getAddress() : "N/A"
            });
        }
    }
    
    /**
     * Selects a supplier from the table and populates the form
     */
    private void selectSupplierFromTable() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0) {
            int supplierId = (Integer) tableModel.getValueAt(selectedRow, 0);
            selectedSupplier = supplierDAO.getSupplierById(supplierId);
            
            if (selectedSupplier != null) {
                nameField.setText(selectedSupplier.getName());
                contactNumberField.setText(selectedSupplier.getContactNumber() != null ? selectedSupplier.getContactNumber() : "");
                emailField.setText(selectedSupplier.getEmail() != null ? selectedSupplier.getEmail() : "");
                addressField.setText(selectedSupplier.getAddress() != null ? selectedSupplier.getAddress() : "");
                
                // Update items supplied list
                updateItemsSuppliedList();
            }
        } else {
            itemsTextArea.setText("");
        }
    }
    
    /**
     * Updates the items supplied list for the selected supplier
     */
    private void updateItemsSuppliedList() {
        if (selectedSupplier != null && itemsTextArea != null) {
            List<Item> allItems = itemDAO.getAllItems();
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Item item : allItems) {
                if (item.getSupplierId() == selectedSupplier.getId()) {
                    sb.append("• ").append(item.getName()).append(" (").append(item.getPartNumber()).append(")\n");
                    count++;
                }
            }
            if (count == 0) {
                itemsTextArea.setText("No items linked to this supplier.");
            } else {
                itemsTextArea.setText(sb.toString());
            }
        }
    }
    
    /**
     * Adds a new supplier
     */
    private void addSupplier() {
        if (!validateForm()) return;
        
        Supplier supplier = createSupplierFromForm();
        if (supplier == null) return;
        
        if (supplierDAO.addSupplier(supplier)) {
            JOptionPane.showMessageDialog(this, "Supplier added successfully!");
            clearForm();
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add supplier.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Updates an existing supplier
     */
    private void updateSupplier() {
        if (selectedSupplier == null) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!validateForm()) return;
        
        Supplier supplier = createSupplierFromForm();
        if (supplier == null) return;
        
        supplier.setId(selectedSupplier.getId());
        
        if (supplierDAO.updateSupplier(supplier)) {
            JOptionPane.showMessageDialog(this, "Supplier updated successfully!");
            clearForm();
            selectedSupplier = null;
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update supplier.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Deletes the selected supplier
     */
    private void deleteSupplier() {
        if (selectedSupplier == null) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this supplier?\n" + selectedSupplier.getName(),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (supplierDAO.deleteSupplier(selectedSupplier.getId())) {
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully!");
                clearForm();
                selectedSupplier = null;
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete supplier.", "Error", JOptionPane.ERROR_MESSAGE);
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
        selectedSupplier = null;
        supplierTable.clearSelection();
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
     * Creates a Supplier object from form fields
     */
    private Supplier createSupplierFromForm() {
        try {
            String name = nameField.getText().trim();
            String contactNumber = contactNumberField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            
            Supplier supplier = new Supplier(
                name,
                contactNumber.isEmpty() ? null : contactNumber,
                email.isEmpty() ? null : email,
                address.isEmpty() ? null : address
            );
            
            return supplier;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating supplier: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}

