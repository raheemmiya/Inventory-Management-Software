package com.garage.inventory.gui;

import com.garage.inventory.dao.ItemDAO;
import com.garage.inventory.dao.SaleDAO;
import com.garage.inventory.dao.CustomerDAO;
import com.garage.inventory.model.Item;
import com.garage.inventory.model.Sale;
import com.garage.inventory.model.Customer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * SalesScreen allows recording sales and automatically decreases stock
 * When items are sold, the stock quantity is decreased automatically
 */
public class SalesScreen extends JPanel {
    private MainApplication mainApp;
    private JComboBox<Item> itemComboBox;
    private JComboBox<Customer> customerComboBox;
    private JTextField quantityField, unitPriceField, totalAmountField;
    private JTextField notesField;
    private JSpinner saleDateSpinner;
    private JTable saleTable;
    private DefaultTableModel tableModel;
    private ItemDAO itemDAO;
    private SaleDAO saleDAO;
    private CustomerDAO customerDAO;
    
    public SalesScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        itemDAO = new ItemDAO();
        saleDAO = new SaleDAO();
        customerDAO = new CustomerDAO();
        
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
        customerComboBox = new JComboBox<>();
        customerComboBox.addItem(new Customer("", "", "", "", "")); // Empty option
        
        quantityField = new JTextField(15);
        unitPriceField = new JTextField(15);
        totalAmountField = new JTextField(15);
        totalAmountField.setEditable(false);
        notesField = new JTextField(15);
        
        // Date spinner for sale date
        SpinnerDateModel dateModel = new SpinnerDateModel();
        saleDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(saleDateSpinner, "yyyy-MM-dd");
        saleDateSpinner.setEditor(dateEditor);
        saleDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        
        // Initialize table
        String[] columns = {"ID", "Item", "Quantity", "Unit Price", "Total", "Date", "Customer"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        saleTable = new JTable(tableModel);
        saleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Calculate total when quantity or price changes
        quantityField.addActionListener(e -> calculateTotal());
        unitPriceField.addActionListener(e -> calculateTotal());
        
        // When item is selected, populate unit price with item's current price
        itemComboBox.addActionListener(e -> {
            Item selectedItem = (Item) itemComboBox.getSelectedItem();
            if (selectedItem != null && selectedItem.getId() > 0) {
                unitPriceField.setText(selectedItem.getUnitPrice().toString());
                calculateTotal();
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
        leftPanel.setBorder(BorderFactory.createTitledBorder("New Sale"));
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Sales History"));
        JScrollPane scrollPane = new JScrollPane(saleTable);
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
        
        // Sale Date
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Sale Date *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(saleDateSpinner, gbc);
        
        // Customer
        gbc.gridx = 0; gbc.gridy = ++row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(customerComboBox, gbc);
        
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
        
        JButton addButton = new JButton("Record Sale");
        addButton.addActionListener(e -> recordSale());
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        
        panel.add(addButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    /**
     * Refreshes data - loads items, customers, and sales
     */
    public void refreshData() {
        // Refresh items combo box
        itemComboBox.removeAllItems();
        List<Item> items = itemDAO.getAllItems();
        for (Item item : items) {
            itemComboBox.addItem(item);
        }
        
        // Refresh customers combo box
        customerComboBox.removeAllItems();
        customerComboBox.addItem(new Customer("", "", "", "", "")); // Empty option
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer customer : customers) {
            customerComboBox.addItem(customer);
        }
        
        // Refresh sales table
        tableModel.setRowCount(0);
        List<Sale> sales = saleDAO.getAllSales();
        for (Sale sale : sales) {
            Item item = itemDAO.getItemById(sale.getItemId());
            Customer customer = sale.getCustomerId() > 0 ? 
                customerDAO.getCustomerById(sale.getCustomerId()) : null;
            
            tableModel.addRow(new Object[]{
                sale.getId(),
                item != null ? item.getName() : "N/A",
                sale.getQuantity(),
                "$" + String.format("%.2f", sale.getUnitPrice()),
                "$" + String.format("%.2f", sale.getTotalAmount()),
                sale.getSaleDate(),
                customer != null ? customer.getName() : "N/A"
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
     * Records a new sale and decreases stock automatically
     */
    private void recordSale() {
        if (!validateForm()) return;
        
        try {
            Item selectedItem = (Item) itemComboBox.getSelectedItem();
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Please select an item.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check stock availability
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (selectedItem.getStockQuantity() < quantity) {
                JOptionPane.showMessageDialog(this, 
                    "Insufficient stock! Available: " + selectedItem.getStockQuantity(),
                    "Stock Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText().trim());
            BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));
            
            // Get date from spinner
            java.util.Date date = (java.util.Date) saleDateSpinner.getValue();
            Date saleDate = new Date(date.getTime());
            
            Customer selectedCustomer = (Customer) customerComboBox.getSelectedItem();
            int customerId = (selectedCustomer != null && selectedCustomer.getId() > 0) ? selectedCustomer.getId() : 0;
            String notes = notesField.getText().trim();
            
            Sale sale = new Sale(selectedItem.getId(), customerId, quantity, unitPrice, totalAmount,
                saleDate, notes.isEmpty() ? null : notes);
            
            // This automatically decreases the stock quantity
            if (saleDAO.addSale(sale)) {
                JOptionPane.showMessageDialog(this, 
                    "Sale recorded successfully!\nStock has been updated automatically.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshData();
                // Refresh dashboard if it's open
                mainApp.showScreen("DASHBOARD");
                mainApp.showScreen("SALES");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to record sale.\nPossible reasons:\n- Insufficient stock\n- Database connection error\n- Item doesn't exist\n\nCheck the console for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error recording sale: " + e.getMessage() + "\n\nCheck the console for details.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clears the form
     */
    private void clearForm() {
        itemComboBox.setSelectedIndex(0);
        customerComboBox.setSelectedIndex(0);
        quantityField.setText("");
        unitPriceField.setText("");
        totalAmountField.setText("");
        notesField.setText("");
        // Reset date to today
        saleDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
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

