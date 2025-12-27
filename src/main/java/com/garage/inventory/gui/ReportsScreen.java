package com.garage.inventory.gui;

import com.garage.inventory.dao.ItemDAO;
import com.garage.inventory.dao.PurchaseDAO;
import com.garage.inventory.dao.SaleDAO;
import com.garage.inventory.dao.SupplierDAO;
import com.garage.inventory.dao.CustomerDAO;
import com.garage.inventory.model.Item;
import com.garage.inventory.model.Purchase;
import com.garage.inventory.model.Sale;
import com.garage.inventory.model.Supplier;
import com.garage.inventory.model.Customer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * ReportsScreen provides various reports including:
 * - Daily stock report
 * - Monthly stock report
 * - Low stock report
 */
public class ReportsScreen extends JPanel {
    private MainApplication mainApp;
    private JComboBox<String> reportTypeComboBox;
    private JSpinner startDateSpinner, endDateSpinner;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private ItemDAO itemDAO;
    private SaleDAO saleDAO;
    private PurchaseDAO purchaseDAO;
    private SupplierDAO supplierDAO;
    private CustomerDAO customerDAO;
    
    public ReportsScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        itemDAO = new ItemDAO();
        saleDAO = new SaleDAO();
        purchaseDAO = new PurchaseDAO();
        supplierDAO = new SupplierDAO();
        customerDAO = new CustomerDAO();
        
        initializeComponents();
        setupLayout();
    }
    
    /**
     * Initializes all GUI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Report type combo box
        String[] reportTypes = {"Low Stock Report", "Daily Sales Report", "Monthly Sales Report", 
                                "Daily Purchase Report", "Monthly Purchase Report", "Stock Report"};
        reportTypeComboBox = new JComboBox<>(reportTypes);
        reportTypeComboBox.addActionListener(e -> generateReport());
        
        // Date spinners
        SpinnerDateModel startDateModel = new SpinnerDateModel();
        startDateSpinner = new JSpinner(startDateModel);
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        
        SpinnerDateModel endDateModel = new SpinnerDateModel();
        endDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        
        // Table
        String[] columns = {"Column1", "Column2", "Column3", "Column4", "Column5"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(tableModel);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        // Top panel with controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Report Options"));
        
        controlPanel.add(new JLabel("Report Type:"));
        controlPanel.add(reportTypeComboBox);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(new JLabel("Start Date:"));
        controlPanel.add(startDateSpinner);
        controlPanel.add(new JLabel("End Date:"));
        controlPanel.add(endDateSpinner);
        
        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        controlPanel.add(generateButton);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Report Results"));
        JScrollPane scrollPane = new JScrollPane(reportTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with total
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(totalLabel);
        tablePanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(controlPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }
    
    /**
     * Refreshes data (same as generate report)
     */
    public void refreshData() {
        generateReport();
    }
    
    /**
     * Generates the selected report based on report type
     */
    private void generateReport() {
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        
        switch (reportType) {
            case "Low Stock Report":
                generateLowStockReport();
                break;
            case "Daily Sales Report":
                generateDailySalesReport();
                break;
            case "Monthly Sales Report":
                generateMonthlySalesReport();
                break;
            case "Daily Purchase Report":
                generateDailyPurchaseReport();
                break;
            case "Monthly Purchase Report":
                generateMonthlyPurchaseReport();
                break;
            case "Stock Report":
                generateStockReport();
                break;
        }
    }
    
    /**
     * Generates low stock report showing items with low inventory
     */
    private void generateLowStockReport() {
        String[] columns = {"Part Number", "Item Name", "Current Stock", "Min Level", "Category", "Unit Price"};
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        List<Item> lowStockItems = itemDAO.getLowStockItems();
        BigDecimal total = BigDecimal.ZERO;
        
        for (Item item : lowStockItems) {
            tableModel.addRow(new Object[]{
                item.getPartNumber(),
                item.getName(),
                item.getStockQuantity(),
                item.getMinStockLevel(),
                item.getCategory() != null ? item.getCategory() : "N/A",
                "$" + String.format("%.2f", item.getUnitPrice())
            });
        }
        
        totalLabel.setText("Total Low Stock Items: " + lowStockItems.size());
    }
    
    /**
     * Generates daily sales report for selected date
     */
    private void generateDailySalesReport() {
        String[] columns = {"ID", "Item Name", "Quantity", "Unit Price", "Total Amount", "Customer", "Date"};
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
        Date sqlStartDate = new Date(startDate.getTime());
        Date sqlEndDate = new Date(startDate.getTime()); // Same day
        
        List<Sale> sales = saleDAO.getSalesByDateRange(sqlStartDate, sqlEndDate);
        BigDecimal total = BigDecimal.ZERO;
        
        for (Sale sale : sales) {
            Item item = itemDAO.getItemById(sale.getItemId());
            Customer customer = sale.getCustomerId() > 0 ? 
                customerDAO.getCustomerById(sale.getCustomerId()) : null;
            total = total.add(sale.getTotalAmount());
            
            tableModel.addRow(new Object[]{
                sale.getId(),
                item != null ? item.getName() : "N/A",
                sale.getQuantity(),
                "$" + String.format("%.2f", sale.getUnitPrice()),
                "$" + String.format("%.2f", sale.getTotalAmount()),
                customer != null ? customer.getName() : "N/A",
                sale.getSaleDate()
            });
        }
        
        totalLabel.setText("Total Sales: $" + String.format("%.2f", total));
    }
    
    /**
     * Generates monthly sales report for date range
     */
    private void generateMonthlySalesReport() {
        String[] columns = {"ID", "Item Name", "Quantity", "Unit Price", "Total Amount", "Customer", "Date"};
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
        java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
        Date sqlStartDate = new Date(startDate.getTime());
        Date sqlEndDate = new Date(endDate.getTime());
        
        List<Sale> sales = saleDAO.getSalesByDateRange(sqlStartDate, sqlEndDate);
        BigDecimal total = BigDecimal.ZERO;
        
        for (Sale sale : sales) {
            Item item = itemDAO.getItemById(sale.getItemId());
            Customer customer = sale.getCustomerId() > 0 ? 
                customerDAO.getCustomerById(sale.getCustomerId()) : null;
            total = total.add(sale.getTotalAmount());
            
            tableModel.addRow(new Object[]{
                sale.getId(),
                item != null ? item.getName() : "N/A",
                sale.getQuantity(),
                "$" + String.format("%.2f", sale.getUnitPrice()),
                "$" + String.format("%.2f", sale.getTotalAmount()),
                customer != null ? customer.getName() : "N/A",
                sale.getSaleDate()
            });
        }
        
        totalLabel.setText("Total Sales: $" + String.format("%.2f", total));
    }
    
    /**
     * Generates daily purchase report for selected date
     */
    private void generateDailyPurchaseReport() {
        String[] columns = {"ID", "Item Name", "Supplier", "Quantity", "Unit Price", "Total Amount", "Date", "Invoice #"};
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
        Date sqlStartDate = new Date(startDate.getTime());
        Date sqlEndDate = new Date(startDate.getTime()); // Same day
        
        List<Purchase> purchases = purchaseDAO.getPurchasesByDateRange(sqlStartDate, sqlEndDate);
        BigDecimal total = BigDecimal.ZERO;
        
        for (Purchase purchase : purchases) {
            Item item = itemDAO.getItemById(purchase.getItemId());
            Supplier supplier = purchase.getSupplierId() > 0 ?
                supplierDAO.getSupplierById(purchase.getSupplierId()) : null;
            total = total.add(purchase.getTotalAmount());
            
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
        
        totalLabel.setText("Total Purchases: $" + String.format("%.2f", total));
    }
    
    /**
     * Generates monthly purchase report for date range
     */
    private void generateMonthlyPurchaseReport() {
        String[] columns = {"ID", "Item Name", "Supplier", "Quantity", "Unit Price", "Total Amount", "Date", "Invoice #"};
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
        java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
        Date sqlStartDate = new Date(startDate.getTime());
        Date sqlEndDate = new Date(endDate.getTime());
        
        List<Purchase> purchases = purchaseDAO.getPurchasesByDateRange(sqlStartDate, sqlEndDate);
        BigDecimal total = BigDecimal.ZERO;
        
        for (Purchase purchase : purchases) {
            Item item = itemDAO.getItemById(purchase.getItemId());
            Supplier supplier = purchase.getSupplierId() > 0 ?
                supplierDAO.getSupplierById(purchase.getSupplierId()) : null;
            total = total.add(purchase.getTotalAmount());
            
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
        
        totalLabel.setText("Total Purchases: $" + String.format("%.2f", total));
    }
    
    /**
     * Generates stock report showing all items and their stock levels
     */
    private void generateStockReport() {
        String[] columns = {"Part Number", "Item Name", "Category", "Stock Quantity", "Min Level", "Unit Price", "Location"};
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
        
        List<Item> items = itemDAO.getAllItems();
        int totalItems = 0;
        
        for (Item item : items) {
            totalItems += item.getStockQuantity();
            tableModel.addRow(new Object[]{
                item.getPartNumber(),
                item.getName(),
                item.getCategory() != null ? item.getCategory() : "N/A",
                item.getStockQuantity(),
                item.getMinStockLevel(),
                "$" + String.format("%.2f", item.getUnitPrice()),
                item.getLocation() != null ? item.getLocation() : "N/A"
            });
        }
        
        totalLabel.setText("Total Items: " + items.size() + " | Total Stock Quantity: " + totalItems);
    }
}

