package com.garage.inventory.gui;

import com.garage.inventory.dao.ItemDAO;
import com.garage.inventory.dao.PurchaseDAO;
import com.garage.inventory.dao.SaleDAO;
import com.garage.inventory.model.Item;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DashboardScreen displays key statistics and low stock alerts
 * Shows: total items, low stock alerts, today's sales, and today's purchases
 */
public class DashboardScreen extends JPanel {
    private MainApplication mainApp;
    private JLabel totalItemsLabel;
    private JLabel lowStockCountLabel;
    private JLabel todaySalesLabel;
    private JLabel todayPurchasesLabel;
    private JTable lowStockTable;
    private DefaultTableModel tableModel;
    private ItemDAO itemDAO;
    private SaleDAO saleDAO;
    private PurchaseDAO purchaseDAO;
    
    public DashboardScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        itemDAO = new ItemDAO();
        saleDAO = new SaleDAO();
        purchaseDAO = new PurchaseDAO();
        
        initializeComponents();
        setupLayout();
        refreshData();
    }
    
    /**
     * Initializes all GUI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Initialize labels
        totalItemsLabel = new JLabel("0", JLabel.CENTER);
        totalItemsLabel.setFont(new Font("Arial", Font.BOLD, 32));
        totalItemsLabel.setForeground(new Color(0, 102, 204));
        
        lowStockCountLabel = new JLabel("0", JLabel.CENTER);
        lowStockCountLabel.setFont(new Font("Arial", Font.BOLD, 32));
        lowStockCountLabel.setForeground(new Color(204, 0, 0));
        
        todaySalesLabel = new JLabel("$0.00", JLabel.CENTER);
        todaySalesLabel.setFont(new Font("Arial", Font.BOLD, 28));
        todaySalesLabel.setForeground(new Color(0, 153, 0));
        
        todayPurchasesLabel = new JLabel("$0.00", JLabel.CENTER);
        todayPurchasesLabel.setFont(new Font("Arial", Font.BOLD, 28));
        todayPurchasesLabel.setForeground(new Color(255, 153, 0));
        
        // Initialize table for low stock items
        String[] columns = {"Part Number", "Item Name", "Current Stock", "Min Level", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lowStockTable = new JTable(tableModel);
        lowStockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lowStockTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    /**
     * Sets up the layout with statistics cards and low stock table
     */
    private void setupLayout() {
        // Top panel with statistics
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsPanel.setBackground(new Color(240, 240, 240));
        
        // Total Items Card
        JPanel totalItemsCard = createStatCard("Total Items in Stock", totalItemsLabel, new Color(230, 240, 255));
        statsPanel.add(totalItemsCard);
        
        // Low Stock Alerts Card
        JPanel lowStockCard = createStatCard("Low Stock Alerts", lowStockCountLabel, new Color(255, 230, 230));
        statsPanel.add(lowStockCard);
        
        // Today's Sales Card
        JPanel todaySalesCard = createStatCard("Today's Sales", todaySalesLabel, new Color(230, 255, 230));
        statsPanel.add(todaySalesCard);
        
        // Today's Purchases Card
        JPanel todayPurchasesCard = createStatCard("Today's Purchases", todayPurchasesLabel, new Color(255, 245, 230));
        statsPanel.add(todayPurchasesCard);
        
        // Low Stock Items Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Low Stock Items",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        tablePanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(lowStockTable);
        scrollPane.setPreferredSize(new Dimension(0, 250));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button to go to inventory management
        JButton manageInventoryButton = new JButton("Manage Inventory");
        manageInventoryButton.addActionListener(e -> mainApp.showScreen("INVENTORY"));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(manageInventoryButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Combine all panels
        add(statsPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a statistics card with label and value
     * 
     * @param title The title of the statistic
     * @param valueLabel The label displaying the value
     * @param bgColor Background color of the card
     * @return A panel representing the stat card
     */
    private JPanel createStatCard(String title, JLabel valueLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(bgColor);
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Refreshes all dashboard data
     * This method is called when the dashboard is displayed
     */
    public void refreshData() {
        // Get total items count
        int totalItems = itemDAO.getTotalItemCount();
        totalItemsLabel.setText(String.valueOf(totalItems));
        
        // Get low stock items
        List<Item> lowStockItems = itemDAO.getLowStockItems();
        lowStockCountLabel.setText(String.valueOf(lowStockItems.size()));
        
        // Get today's sales
        BigDecimal todaySales = saleDAO.getTodaySalesTotal();
        todaySalesLabel.setText("$" + String.format("%.2f", todaySales));
        
        // Get today's purchases
        BigDecimal todayPurchases = purchaseDAO.getTodayPurchasesTotal();
        todayPurchasesLabel.setText("$" + String.format("%.2f", todayPurchases));
        
        // Update low stock table
        tableModel.setRowCount(0);
        for (Item item : lowStockItems) {
            tableModel.addRow(new Object[]{
                item.getPartNumber(),
                item.getName(),
                item.getStockQuantity(),
                item.getMinStockLevel(),
                item.getCategory() != null ? item.getCategory() : "N/A"
            });
        }
    }
}

