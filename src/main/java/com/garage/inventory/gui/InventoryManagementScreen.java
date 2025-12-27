package com.garage.inventory.gui;

import com.garage.inventory.dao.ItemDAO;
import com.garage.inventory.dao.SupplierDAO;
import com.garage.inventory.model.Item;
import com.garage.inventory.model.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * InventoryManagementScreen allows managing spare parts/items
 * Features: Add, Edit, Delete items, View stock quantities in a table
 */
public class InventoryManagementScreen extends JPanel {
    private MainApplication mainApp;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JTextField partNumberField, nameField, descriptionField, categoryField;
    private JTextField unitPriceField, stockQuantityField, minStockLevelField, locationField;
    private JComboBox<Supplier> supplierComboBox;
    private ItemDAO itemDAO;
    private SupplierDAO supplierDAO;
    private Item selectedItem;
    
    public InventoryManagementScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        itemDAO = new ItemDAO();
        supplierDAO = new SupplierDAO();
        
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
        partNumberField = new JTextField(20);
        nameField = new JTextField(20);
        descriptionField = new JTextField(20);
        categoryField = new JTextField(20);
        unitPriceField = new JTextField(20);
        stockQuantityField = new JTextField(20);
        minStockLevelField = new JTextField(20);
        locationField = new JTextField(20);
        supplierComboBox = new JComboBox<>();
        supplierComboBox.addItem(new Supplier("", "", "", "")); // Empty option
        
        // Initialize table
        String[] columns = {"ID", "Part Number", "Name", "Category", "Unit Price", 
                           "Stock Qty", "Min Level", "Location", "Supplier"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemTable = new JTable(tableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectItemFromTable();
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
        leftPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Items List"));
        JScrollPane scrollPane = new JScrollPane(itemTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh button for table
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        JPanel refreshPanel = new JPanel();
        refreshPanel.add(refreshButton);
        tablePanel.add(refreshPanel, BorderLayout.SOUTH);
        
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
        
        // Part Number
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Part Number *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(partNumberField, gbc);
        
        // Name
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Name *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nameField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(descriptionField, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(categoryField, gbc);
        
        // Unit Price
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Unit Price *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(unitPriceField, gbc);
        
        // Stock Quantity
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Stock Quantity *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(stockQuantityField, gbc);
        
        // Min Stock Level
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Min Stock Level *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(minStockLevelField, gbc);
        
        // Location
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(locationField, gbc);
        
        // Supplier
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(supplierComboBox, gbc);
        
        return panel;
    }
    
    /**
     * Creates the button panel with action buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Add New");
        addButton.addActionListener(e -> addItem());
        
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> updateItem());
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteItem());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Refreshes the data - loads all items and suppliers
     */
    public void refreshData() {
        // Refresh suppliers combo box
        supplierComboBox.removeAllItems();
        supplierComboBox.addItem(new Supplier("", "", "", "")); // Empty option
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierComboBox.addItem(supplier);
        }
        
        // Refresh items table
        tableModel.setRowCount(0);
        List<Item> items = itemDAO.getAllItems();
        for (Item item : items) {
            Supplier supplier = item.getSupplierId() > 0 ? 
                supplierDAO.getSupplierById(item.getSupplierId()) : null;
            String supplierName = supplier != null ? supplier.getName() : "N/A";
            
            tableModel.addRow(new Object[]{
                item.getId(),
                item.getPartNumber(),
                item.getName(),
                item.getCategory() != null ? item.getCategory() : "N/A",
                "$" + String.format("%.2f", item.getUnitPrice()),
                item.getStockQuantity(),
                item.getMinStockLevel(),
                item.getLocation() != null ? item.getLocation() : "N/A",
                supplierName
            });
        }
    }
    
    /**
     * Selects an item from the table and populates the form
     */
    private void selectItemFromTable() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow >= 0) {
            int itemId = (Integer) tableModel.getValueAt(selectedRow, 0);
            selectedItem = itemDAO.getItemById(itemId);
            
            if (selectedItem != null) {
                partNumberField.setText(selectedItem.getPartNumber());
                nameField.setText(selectedItem.getName());
                descriptionField.setText(selectedItem.getDescription() != null ? selectedItem.getDescription() : "");
                categoryField.setText(selectedItem.getCategory() != null ? selectedItem.getCategory() : "");
                unitPriceField.setText(selectedItem.getUnitPrice().toString());
                stockQuantityField.setText(String.valueOf(selectedItem.getStockQuantity()));
                minStockLevelField.setText(String.valueOf(selectedItem.getMinStockLevel()));
                locationField.setText(selectedItem.getLocation() != null ? selectedItem.getLocation() : "");
                
                // Select supplier in combo box
                if (selectedItem.getSupplierId() > 0) {
                    for (int i = 0; i < supplierComboBox.getItemCount(); i++) {
                        Supplier s = supplierComboBox.getItemAt(i);
                        if (s.getId() == selectedItem.getSupplierId()) {
                            supplierComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    supplierComboBox.setSelectedIndex(0);
                }
            }
        }
    }
    
    /**
     * Adds a new item to the inventory
     */
    private void addItem() {
        if (!validateForm()) return;
        
        Item item = createItemFromForm();
        if (item == null) return;
        
        // Check if part number already exists
        if (itemDAO.partNumberExists(item.getPartNumber())) {
            JOptionPane.showMessageDialog(this, 
                "Part number already exists. Please use a different part number.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (itemDAO.addItem(item)) {
            JOptionPane.showMessageDialog(this, "Item added successfully!");
            clearForm();
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add item.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Updates an existing item
     */
    private void updateItem() {
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!validateForm()) return;
        
        Item item = createItemFromForm();
        if (item == null) return;
        
        item.setId(selectedItem.getId());
        
        // Check part number if it's changed
        if (!item.getPartNumber().equals(selectedItem.getPartNumber()) && 
            itemDAO.partNumberExists(item.getPartNumber())) {
            JOptionPane.showMessageDialog(this, 
                "Part number already exists. Please use a different part number.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (itemDAO.updateItem(item)) {
            JOptionPane.showMessageDialog(this, "Item updated successfully!");
            clearForm();
            selectedItem = null;
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update item.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Deletes the selected item
     */
    private void deleteItem() {
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this item?\n" + selectedItem.getName(),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (itemDAO.deleteItem(selectedItem.getId())) {
                JOptionPane.showMessageDialog(this, "Item deleted successfully!");
                clearForm();
                selectedItem = null;
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Clears the form fields
     */
    private void clearForm() {
        partNumberField.setText("");
        nameField.setText("");
        descriptionField.setText("");
        categoryField.setText("");
        unitPriceField.setText("");
        stockQuantityField.setText("");
        minStockLevelField.setText("");
        locationField.setText("");
        supplierComboBox.setSelectedIndex(0);
        selectedItem = null;
        itemTable.clearSelection();
    }
    
    /**
     * Validates the form inputs
     */
    private boolean validateForm() {
        if (partNumberField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Part number is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            new BigDecimal(unitPriceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid unit price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            Integer.parseInt(stockQuantityField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid stock quantity.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            Integer.parseInt(minStockLevelField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid min stock level.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Creates an Item object from form fields
     */
    private Item createItemFromForm() {
        try {
            String partNumber = partNumberField.getText().trim();
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            String category = categoryField.getText().trim();
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText().trim());
            int stockQuantity = Integer.parseInt(stockQuantityField.getText().trim());
            int minStockLevel = Integer.parseInt(minStockLevelField.getText().trim());
            String location = locationField.getText().trim();
            
            Supplier selectedSupplier = (Supplier) supplierComboBox.getSelectedItem();
            int supplierId = (selectedSupplier != null && selectedSupplier.getId() > 0) ? selectedSupplier.getId() : 0;
            
            Item item = new Item(partNumber, name, description.isEmpty() ? null : description,
                               category.isEmpty() ? null : category, unitPrice, stockQuantity,
                               minStockLevel, location.isEmpty() ? null : location, supplierId);
            
            return item;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating item: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}

