
package com.garage.inventory.gui;

import com.garage.inventory.dao.CustomerDAO;
import com.garage.inventory.dao.DebtDAO;
import com.garage.inventory.model.Customer;
import com.garage.inventory.model.DebtTransaction;
import com.garage.inventory.model.DebtPayment;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

public class DebtScreen extends JPanel {
    private MainApplication mainApp;
    private JTable debtTable;
    private DefaultTableModel debtTableModel;
    private JComboBox<Customer> customerComboBox;
    private JComboBox<Customer> paymentCustomerComboBox;
    private JComboBox<String> statusFilterCombo;
    private JTextField amountField, referenceField, notesField;
    private JTextField paymentAmountField, paymentReferenceField, paymentNotesField;
    private JSpinner dueDateSpinner, paymentDateSpinner;
    private JComboBox<String> paymentMethodCombo;
    private JComboBox<String> paymentMethodPaymentCombo;
    private JLabel totalOutstandingLabel, overdueCountLabel;
    private JTable paymentHistoryTable;
    private DefaultTableModel paymentHistoryTableModel;
    private DebtDAO debtDAO;
    private CustomerDAO customerDAO;
    private DebtTransaction selectedDebt;

    public DebtScreen(MainApplication mainApp) {
        this.mainApp = mainApp;
        debtDAO = new DebtDAO();
        customerDAO = new CustomerDAO();

        initializeComponents();
        setupLayout();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));

        customerComboBox = new JComboBox<>();
        paymentCustomerComboBox = new JComboBox<>();
        statusFilterCombo = new JComboBox<>(new String[]{"All", "Pending", "Partial", "Overdue", "Paid"});
        paymentMethodCombo = new JComboBox<>(new String[]{"CASH", "BANK_TRANSFER", "CHEQUE", "MOBILE_MONEY", "CARD"});
        paymentMethodPaymentCombo = new JComboBox<>(new String[]{"CASH", "BANK_TRANSFER", "CHEQUE", "MOBILE_MONEY", "CARD"});

        amountField = new JTextField(15);
        referenceField = new JTextField(15);
        notesField = new JTextField(15);
        paymentAmountField = new JTextField(15);
        paymentReferenceField = new JTextField(15);
        paymentNotesField = new JTextField(15);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        dueDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "yyyy-MM-dd");
        dueDateSpinner.setEditor(dateEditor);
        dueDateSpinner.setValue(java.util.Calendar.getInstance().getTime());

        SpinnerDateModel paymentDateModel = new SpinnerDateModel();
        paymentDateSpinner = new JSpinner(paymentDateModel);
        JSpinner.DateEditor paymentDateEditor = new JSpinner.DateEditor(paymentDateSpinner, "yyyy-MM-dd");
        paymentDateSpinner.setEditor(paymentDateEditor);
        paymentDateSpinner.setValue(java.util.Calendar.getInstance().getTime());

        totalOutstandingLabel = new JLabel("$0.00", JLabel.CENTER);
        totalOutstandingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalOutstandingLabel.setForeground(new Color(204, 0, 0));

        overdueCountLabel = new JLabel("0", JLabel.CENTER);
        overdueCountLabel.setFont(new Font("Arial", Font.BOLD, 24));
        overdueCountLabel.setForeground(new Color(255, 102, 0));

        String[] columns = {"ID", "Customer", "Type", "Amount", "Balance", "Date", "Due Date", "Status"};
        debtTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        debtTable = new JTable(debtTableModel);
        debtTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        debtTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        debtTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectDebtFromTable();
            }
        });

        String[] paymentColumns = {"ID", "Amount", "Date", "Method", "Reference", "Notes"};
        paymentHistoryTableModel = new DefaultTableModel(paymentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        paymentHistoryTable = new JTable(paymentHistoryTableModel);
        paymentHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentHistoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        statusFilterCombo.addActionListener(e -> filterDebts());
    }

    private void setupLayout() {
        JPanel summaryPanel = createSummaryPanel();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Debt Transactions"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(statusFilterCombo);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        filterPanel.add(refreshButton);

        tablePanel.add(filterPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(debtTable), BorderLayout.CENTER);

        JPanel actionsPanel = createActionsPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, actionsPanel);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.7);

        add(summaryPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 240, 240));

        JPanel outstandingCard = createStatCard("Total Outstanding Debt", totalOutstandingLabel, new Color(255, 230, 230));
        panel.add(outstandingCard);

        JPanel overdueCard = createStatCard("Overdue Transactions", overdueCountLabel, new Color(255, 240, 220));
        panel.add(overdueCard);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(bgColor);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 0));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("New Credit Sale", createCreditSalePanel());
        tabbedPane.addTab("Record Payment", createPaymentPanel());
        tabbedPane.addTab("Payment History", createPaymentHistoryPanel());

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCreditSalePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Customer *:"), gbc);
        gbc.gridx = 1;
        panel.add(customerComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Amount *:"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        panel.add(dueDateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        panel.add(paymentMethodCombo, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Reference #:"), gbc);
        gbc.gridx = 1;
        panel.add(referenceField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        panel.add(notesField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton recordButton = new JButton("Record Credit Sale");
        recordButton.addActionListener(e -> recordCreditSale());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearCreditSaleForm());
        buttonPanel.add(recordButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Select Debt:"), gbc);
        gbc.gridx = 1;
        JLabel debtInfoLabel = new JLabel("Select a debt from the table");
        panel.add(debtInfoLabel, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Payment Amount *:"), gbc);
        gbc.gridx = 1;
        panel.add(paymentAmountField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Payment Date:"), gbc);
        gbc.gridx = 1;
        panel.add(paymentDateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Payment Method *:"), gbc);
        gbc.gridx = 1;
        panel.add(paymentMethodPaymentCombo, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Reference #:"), gbc);
        gbc.gridx = 1;
        panel.add(paymentReferenceField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        panel.add(paymentNotesField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton recordButton = new JButton("Record Payment");
        recordButton.addActionListener(e -> recordPayment());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearPaymentForm());
        buttonPanel.add(recordButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createPaymentHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(paymentHistoryTable), BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        // Update statistics
        BigDecimal totalOutstanding = debtDAO.getTotalOutstandingDebt();
        totalOutstandingLabel.setText("$" + String.format("%.2f", totalOutstanding));

        List<DebtTransaction> overdueDebts = debtDAO.getOverdueDebts();
        overdueCountLabel.setText(String.valueOf(overdueDebts.size()));

        // Refresh customers
        customerComboBox.removeAllItems();
        paymentCustomerComboBox.removeAllItems();
        List<Customer> customers = customerDAO.getAllCustomers();
        for (Customer customer : customers) {
            customerComboBox.addItem(customer);
            paymentCustomerComboBox.addItem(customer);
        }

        // Refresh debt transactions
        filterDebts();
    }

    private void filterDebts() {
        debtTableModel.setRowCount(0);
        String filter = (String) statusFilterCombo.getSelectedItem();

        List<DebtTransaction> debts;
        if ("All".equals(filter)) {
            debts = debtDAO.getAllDebtTransactions();
        } else if ("Overdue".equals(filter)) {
            debts = debtDAO.getOverdueDebts();
        } else {
            debts = debtDAO.getPendingDebts();
        }

        for (DebtTransaction debt : debts) {
            Customer customer = customerDAO.getCustomerById(debt.getCustomerId());
            debtTableModel.addRow(new Object[]{
                    debt.getId(),
                    customer != null ? customer.getName() : "N/A",
                    debt.getTransactionType().name(),
                    "$" + String.format("%.2f", debt.getAmount()),
                    "$" + String.format("%.2f", debt.getRemainingBalance()),
                    debt.getTransactionDate(),
                    debt.getDueDate() != null ? debt.getDueDate() : "N/A",
                    debt.getStatus().name()
            });
        }
    }

    private void selectDebtFromTable() {
        int selectedRow = debtTable.getSelectedRow();
        if (selectedRow >= 0) {
            int debtId = (Integer) debtTableModel.getValueAt(selectedRow, 0);
            List<DebtTransaction> allDebts = debtDAO.getAllDebtTransactions();
            for (DebtTransaction debt : allDebts) {
                if (debt.getId() == debtId) {
                    selectedDebt = debt;
                    loadPaymentHistory();
                    break;
                }
            }
        }
    }

    private void loadPaymentHistory() {
        paymentHistoryTableModel.setRowCount(0);
        if (selectedDebt != null) {
            List<DebtPayment> payments = debtDAO.getPaymentHistory(selectedDebt.getId());
            for (DebtPayment payment : payments) {
                paymentHistoryTableModel.addRow(new Object[]{
                        payment.getId(),
                        "$" + String.format("%.2f", payment.getPaymentAmount()),
                        payment.getPaymentDate(),
                        payment.getPaymentMethod(),
                        payment.getReferenceNumber() != null ? payment.getReferenceNumber() : "N/A",
                        payment.getNotes() != null ? payment.getNotes() : ""
                });
            }
        }
    }

    private void recordCreditSale() {
        try {
            Customer customer = (Customer) customerComboBox.getSelectedItem();
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Please select a customer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            java.util.Date dueDate = (java.util.Date) dueDateSpinner.getValue();
            Date sqlDueDate = new Date(dueDate.getTime());
            Date today = new Date(System.currentTimeMillis());
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            String reference = referenceField.getText().trim();
            String notes = notesField.getText().trim();

            DebtTransaction debt = new DebtTransaction(
                    customer.getId(),
                    null,
                    DebtTransaction.TransactionType.CREDIT_SALE,
                    amount,
                    amount,
                    today,
                    sqlDueDate,
                    paymentMethod,
                    reference.isEmpty() ? null : reference,
                    notes.isEmpty() ? null : notes,
                    DebtTransaction.DebtStatus.PENDING
            );

            if (debtDAO.addDebtTransaction(debt)) {
                JOptionPane.showMessageDialog(this, "Credit sale recorded successfully!");
                clearCreditSaleForm();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record credit sale.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recordPayment() {
        if (selectedDebt == null) {
            JOptionPane.showMessageDialog(this, "Please select a debt from the table.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BigDecimal paymentAmount = new BigDecimal(paymentAmountField.getText().trim());
            if (paymentAmount.compareTo(selectedDebt.getRemainingBalance()) > 0) {
                JOptionPane.showMessageDialog(this,
                        "Payment amount cannot exceed remaining balance ($" +
                                String.format("%.2f", selectedDebt.getRemainingBalance()) + ")",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            java.util.Date paymentDate = (java.util.Date) paymentDateSpinner.getValue();
            Date sqlPaymentDate = new Date(paymentDate.getTime());
            String paymentMethod = (String) paymentMethodPaymentCombo.getSelectedItem();
            String reference = paymentReferenceField.getText().trim();
            String notes = paymentNotesField.getText().trim();

            DebtPayment payment = new DebtPayment(
                    selectedDebt.getId(),
                    paymentAmount,
                    sqlPaymentDate,
                    paymentMethod,
                    reference.isEmpty() ? null : reference,
                    notes.isEmpty() ? null : notes,
                    "admin"
            );

            if (debtDAO.recordPayment(payment)) {
                JOptionPane.showMessageDialog(this, "Payment recorded successfully!");
                clearPaymentForm();
                selectedDebt = null;
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record payment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCreditSaleForm() {
        amountField.setText("");
        referenceField.setText("");
        notesField.setText("");
        dueDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        paymentMethodCombo.setSelectedIndex(0);
    }

    private void clearPaymentForm() {
        paymentAmountField.setText("");
        paymentReferenceField.setText("");
        paymentNotesField.setText("");
        paymentDateSpinner.setValue(java.util.Calendar.getInstance().getTime());
        paymentMethodPaymentCombo.setSelectedIndex(0);
    }
}
