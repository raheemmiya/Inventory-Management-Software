package com.garage.inventory.gui;

import com.garage.inventory.dao.ItemDAO;
import com.garage.inventory.dao.PurchaseDAO;
import com.garage.inventory.dao.SupplierDAO;
import com.garage.inventory.model.Item;
import com.garage.inventory.model.Purchase;
import com.garage.inventory.model.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * PurchaseScreen allows recording purchases and automatically updates stock
 * When items are purchased, the stock quantity is increased automatically
 */
public class PurchaseScreen extends JPanel {
    private MainApplication mainApp;
    private JComboBox<Item> itemComboBox;
    private JComboBox<Supplier> supplierComboBox;
    private JTextField quantityField, unitPriceField, totalAmountField;
    private JTextField invoiceNumberField, notesField;
    private JSpinner purchaseDateSpinner;
    private JTable purchaseTable;
    private DefaultTableModel tableModel;
    private ItemDAO itemDAO;
    private SupplierDAO supplierDAO;
    private PurchaseDAO purchaseDAO;
    
    public PurchaseScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        itemDAO = new ItemDAO();
        supplierDAO = new SupplierDAO();
        purchaseDAO = new PurchaseDAO();
        
        initializeComponents();
        setupLayout();
        refreshData();
    }
    
    /**
     * Initializes all GUI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        
        itemComboBox = new JComboBox<>();
        supplierComboBox = new JComboBox<>();
        supplierComboBox.addItem(new Supplier("", "", "", "")); // Empty option
        
        quantityField = new JTextField(15);
        unitPriceField = new JTextField(15);
        totalAmountField = new JTextField(15);
        totalAmountField.setEditable(false);
        invoiceNumberField = new JTextField(15);
        notesField = new JTextField(15);
        
        // Date spinner for purchase date
        SpinnerDateModel dateModel = new SpinnerDateModel();
        purchaseDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(purchaseDateSpinner, "yyyy-MM-dd");
        purchaseDateSpinner.setEditor(dateEditor);
        purchaseDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        
        // Initialize table
        String[] columns = {"ID", "Item", "Supplier", "Quantity", "Unit Price", 
                           "Total", "Date", "Invoice #"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        purchaseTable = new JTable(tableModel);
        purchaseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        purchaseTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Calculate total when quantity or price changes
        quantityField.addActionListener(e -> calculateTotal());
        unitPriceField.addActionListener(e -> calculateTotal());
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
        leftPanel.setBorder(BorderFactory.createTitledBorder("New Purchase"));
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Purchase History"));
        JScrollPane scrollPane = new JScrollPane(purchaseTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh button
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
     * Creates the form panel
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Item
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Item *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(itemComboBox, gbc);
        
        // Supplier
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Supplier:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(supplierComboBox, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Quantity *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(quantityField, gbc);
        
        // Unit Price
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Unit Price *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(unitPriceField, gbc);
        
        // Total Amount
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(totalAmountField, gbc);
        
        // Purchase Date
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Purchase Date *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(purchaseDateSpinner, gbc);
        
        // Invoice Number
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Invoice Number:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(invoiceNumberField, gbc);
        
        // Notes
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(notesField, gbc);
        
        return panel;
    }
    
    /**
     * Creates button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Record Purchase");
        addButton.addActionListener(e -> recordPurchase());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        panel.add(addButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Refreshes data - loads items, suppliers, and purchases
     */
    public void refreshData() {
        // Refresh items combo box
        itemComboBox.removeAllItems();
        List<Item> items = itemDAO.getAllItems();
        for (Item item : items) {
            itemComboBox.addItem(item);
        }
        
        // Refresh suppliers combo box
        supplierComboBox.removeAllItems();
        supplierComboBox.addItem(new Supplier("", "", "", ""));
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierComboBox.addItem(supplier);
        }
        
        // Refresh purchases table
        tableModel.setRowCount(0);
        List<Purchase> purchases = purchaseDAO.getAllPurchases();
        for (Purchase purchase : purchases) {
            Item item = itemDAO.getItemById(purchase.getItemId());
            Supplier supplier = purchase.getSupplierId() > 0 ? 
                supplierDAO.getSupplierById(purchase.getSupplierId()) : null;
            
            tableModel.addRow(new Object[]{
                purchase.getId(),
                item != null ? item.getName() : "N/A",
                supplier != null ? supplier.getName() : "N/A",
                purchase.getQuantity(),
                "$" + String.format("%.2f", purchase.getUnitPrice()),
                "$" + String.format("%.2f", purchase.getTotalAmount()),
                purchase.getPurchaseDate(),
                purchase.getInvoiceNumber() != null ? purchase.getInvoiceNumber() : "N/A"
            });
        }
    }
    
    /**
     * Calculates total amount based on quantity and unit price
     */
    private void calculateTotal() {
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText().trim());
            BigDecimal total = unitPrice.multiply(new BigDecimal(quantity));
            totalAmountField.setText(total.toString());
        } catch (Exception e) {
            totalAmountField.setText("");
        }
    }
    
    /**
     * Records a new purchase and updates stock automatically
     */
    private void recordPurchase() {
        if (!validateForm()) return;
        
        try {
            Item selectedItem = (Item) itemComboBox.getSelectedItem();
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Please select an item.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Supplier selectedSupplier = (Supplier) supplierComboBox.getSelectedItem();
            int supplierId = (selectedSupplier != null && selectedSupplier.getId() > 0) ? selectedSupplier.getId() : 0;
            
            int quantity = Integer.parseInt(quantityField.getText().trim());
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText().trim());
            BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));
            
            // Get date from spinner
            java.util.Date date = (java.util.Date) purchaseDateSpinner.getValue();
            Date purchaseDate = new Date(date.getTime());
            
            String invoiceNumber = invoiceNumberField.getText().trim();
            String notes = notesField.getText().trim();
            
            Purchase purchase = new Purchase(selectedItem.getId(), supplierId, quantity, 
                unitPrice, totalAmount, purchaseDate, 
                invoiceNumber.isEmpty() ? null : invoiceNumber,
                notes.isEmpty() ? null : notes);
            
            // This automatically updates the stock quantity
            if (purchaseDAO.addPurchase(purchase)) {
                JOptionPane.showMessageDialog(this, 
                    "Purchase recorded successfully!\nStock has been updated automatically.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshData();
                // Refresh dashboard if it's open
                mainApp.showScreen("DASHBOARD");
                mainApp.showScreen("PURCHASE");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to record purchase.\nPlease check:\n- Database connection\n- Item exists in inventory\n- All required fields are filled",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error recording purchase: " + e.getMessage() + "\n\nCheck the console for details.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clears the form
     */
    private void clearForm() {
        itemComboBox.setSelectedIndex(0);
        supplierComboBox.setSelectedIndex(0);
        quantityField.setText("");
        unitPriceField.setText("");
        totalAmountField.setText("");
        invoiceNumberField.setText("");
        notesField.setText("");
        // Reset date to today
        purchaseDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
    }
    
    /**
     * Validates form inputs
     */
    private boolean validateForm() {
        if (itemComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            BigDecimal price = new BigDecimal(unitPriceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Unit price must be greater than 0.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid unit price.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}

