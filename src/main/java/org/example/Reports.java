package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class Reports extends JPanel {

    private final SalesReportTab salesTab;
    private final ItemWiseReportTab itemWiseTab;
    private final LowStockReportTab lowStockTab;
    private final ExpiryReportTab expiryTab;

    public Reports() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UIStyle.BG_LIGHT);
        add(UIStyle.headerLabel("Reports"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIStyle.FONT_LABEL);

        salesTab = new SalesReportTab();
        itemWiseTab = new ItemWiseReportTab();
        lowStockTab = new LowStockReportTab();
        expiryTab = new ExpiryReportTab();

        tabs.addTab("Sales Report", salesTab);
        tabs.addTab("Item-Wise Report", itemWiseTab);
        tabs.addTab("Low Stock Report", lowStockTab);
        tabs.addTab("Expiry Report", expiryTab);

        add(tabs, BorderLayout.CENTER);
    }

    // Package-private accessors
    SalesReportTab getSalesTab() { return salesTab; }
    ItemWiseReportTab getItemWiseTab() { return itemWiseTab; }
    LowStockReportTab getLowStockTab() { return lowStockTab; }
    ExpiryReportTab getExpiryTab() { return expiryTab; }

    ///////////////////////
    // TAB 1: Sales Report
    ///////////////////////
    static class SalesReportTab extends JPanel {
        JTextField fromField, toField;
        JButton generateBtn, printBtn;
        JTable table;
        JLabel summaryLabel;

        SalesReportTab() {
            setLayout(new BorderLayout(10, 10));
            setBackground(UIStyle.BG_LIGHT);

            JPanel filterPanel = new JPanel();
            filterPanel.setOpaque(false);
            LocalDate today = LocalDate.now();
            fromField = new JTextField(today.withDayOfMonth(1).toString(), 10);
            toField = new JTextField(today.toString(), 10);
            generateBtn = UIStyle.styledButton("Generate", UIStyle.PRIMARY);
            printBtn = UIStyle.styledButton("Print", UIStyle.ACCENT);
            generateBtn.addActionListener(e -> generate());
            printBtn.addActionListener(e -> {
                try {
                    table.print(JTable.PrintMode.FIT_WIDTH, null, null);
                } catch (java.awt.print.PrinterException ex) {
                    JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            filterPanel.add(new JLabel("From (yyyy-MM-dd):"));
            filterPanel.add(fromField);
            filterPanel.add(new JLabel("To (yyyy-MM-dd):"));
            filterPanel.add(toField);
            filterPanel.add(generateBtn);
            filterPanel.add(printBtn);

            table = new JTable(new DefaultTableModel(new String[]{"Sale ID", "Date/Time", "Cashier", "Total (R)"}, 0));
            UIStyle.styleTable(table);

            summaryLabel = new JLabel(" ");
            summaryLabel.setFont(UIStyle.FONT_LABEL);

            add(filterPanel, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(summaryLabel, BorderLayout.SOUTH);

            generate();
        }

        private void generate() {
            try {
                java.sql.Date from = java.sql.Date.valueOf(fromField.getText().trim());
                java.sql.Date to = java.sql.Date.valueOf(toField.getText().trim());
                table.setModel(DBConnection.getSalesReport(from, to));
                UIStyle.styleTable(table);
                DBConnection.SalesSummary summary = DBConnection.getSalesSummary(from, to);
                summaryLabel.setText(String.format("  Total Transactions: %d   |   Total Revenue: R%.2f",
                        summary.numSales, summary.revenue));
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ///////////////////////////
    // TAB 2: Item-Wise Report
    ///////////////////////////
    static class ItemWiseReportTab extends JPanel {
        JTextField fromField, toField;
        JButton generateBtn, printBtn;
        JTable table;

        ItemWiseReportTab() {
            setLayout(new BorderLayout(10, 10));
            setBackground(UIStyle.BG_LIGHT);

            JPanel filterPanel = new JPanel();
            filterPanel.setOpaque(false);
            LocalDate today = LocalDate.now();
            fromField = new JTextField(today.withDayOfMonth(1).toString(), 10);
            toField = new JTextField(today.toString(), 10);
            generateBtn = UIStyle.styledButton("Generate", UIStyle.PRIMARY);
            printBtn = UIStyle.styledButton("Print", UIStyle.ACCENT);
            generateBtn.addActionListener(e -> {
                try {
                    table.setModel(DBConnection.getItemWiseReport(
                            java.sql.Date.valueOf(fromField.getText().trim()),
                            java.sql.Date.valueOf(toField.getText().trim())));
                    UIStyle.styleTable(table);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            printBtn.addActionListener(e -> {
                try {
                    table.print(JTable.PrintMode.FIT_WIDTH, null, null);
                } catch (java.awt.print.PrinterException ex) {
                    JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            filterPanel.add(new JLabel("From:"));
            filterPanel.add(fromField);
            filterPanel.add(new JLabel("To:"));
            filterPanel.add(toField);
            filterPanel.add(generateBtn);
            filterPanel.add(printBtn);

            table = new JTable(new DefaultTableModel(new String[]{"Medicine", "Qty Sold", "Revenue (R)"}, 0));
            UIStyle.styleTable(table);

            add(filterPanel, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);

            generateBtn.doClick();
        }
    }

    ////////////////////////////
    // TAB 3: Low Stock Report
    ////////////////////////////
    static class LowStockReportTab extends JPanel {
        JButton refreshBtn, printBtn;
        JTable table;

        LowStockReportTab() {
            setLayout(new BorderLayout(10, 10));
            setBackground(UIStyle.BG_LIGHT);

            JPanel top = new JPanel();
            top.setOpaque(false);
            refreshBtn = UIStyle.styledButton("Refresh", UIStyle.PRIMARY);
            printBtn = UIStyle.styledButton("Print", UIStyle.ACCENT);

            top.add(refreshBtn);
            top.add(printBtn);

            table = new JTable(new DefaultTableModel(
                    new String[]{"Medicine", "Company", "In Stock", "Reorder Level", "Supplier"}, 0));
            UIStyle.styleTable(table);

            add(top, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
        }
    }

    ////////////////////////
    // TAB 4: Expiry Report
    ////////////////////////
    static class ExpiryReportTab extends JPanel {
        JSpinner daysSpinner;
        JButton refreshBtn, printBtn;
        JTable table;

        ExpiryReportTab() {
            setLayout(new BorderLayout(10, 10));
            setBackground(UIStyle.BG_LIGHT);

            JPanel top = new JPanel();
            top.setOpaque(false);
            top.add(new JLabel("Show medicines expiring within (days):"));
            daysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
            top.add(daysSpinner);
            refreshBtn = UIStyle.styledButton("Refresh", UIStyle.PRIMARY);
            printBtn = UIStyle.styledButton("Print", UIStyle.ACCENT);

            top.add(refreshBtn);
            top.add(printBtn);

            table = new JTable(new DefaultTableModel(
                    new String[]{"Medicine", "Company", "Expiry Date", "Qty In Stock", "Supplier"}, 0));
            UIStyle.styleTable(table);

            add(top, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
        }
    }
}