package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PointOfSales extends JPanel {

    private JTable medicineTable, cartTable;
    private DefaultTableModel medicineModel, cartModel;
    private JTextField searchField, qtyField;
    private JLabel totalLabel;
    private JButton searchBtn, clearSearchBtn, addToCartBtn, removeBtn, clearCartBtn, checkoutBtn;

    public PointOfSales() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UIStyle.BG_LIGHT);

        add(buildTopSearchPanel(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMedicineListPanel(), buildCartPanel());
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildTopSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(UIStyle.headerLabel("Point of Sale"), BorderLayout.WEST);

        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        searchField = new JTextField(18);
        searchField.setFont(UIStyle.FONT_FIELD);
        searchBtn = UIStyle.styledButton("Search", UIStyle.PRIMARY);
        clearSearchBtn = UIStyle.styledButton("Clear", Color.GRAY);

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearSearchBtn);
        panel.add(searchPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildMedicineListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createTitledBorder("Available Medicines"));

        String[] cols = {"Name", "Company", "Price (R)", "In Stock"};
        medicineModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        medicineTable = new JTable(medicineModel);
        UIStyle.styleTable(medicineTable);
        panel.add(new JScrollPane(medicineTable), BorderLayout.CENTER);

        JPanel addRow = new JPanel();
        addRow.setOpaque(false);
        qtyField = new JTextField("1", 4);
        addToCartBtn = UIStyle.styledButton("Add to Cart", UIStyle.SUCCESS);
        addRow.add(new JLabel("Qty:"));
        addRow.add(qtyField);
        addRow.add(addToCartBtn);
        panel.add(addRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder("Cart"));

        String[] cols = {"Medicine", "Qty", "Unit Price (R)", "Line Total (R)"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        cartTable = new JTable(cartModel);
        UIStyle.styleTable(cartTable);
        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        totalLabel = new JLabel("Total: R0.00");
        totalLabel.setFont(UIStyle.FONT_HEADER);
        totalLabel.setForeground(UIStyle.PRIMARY_DARK);
        bottom.add(totalLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        removeBtn = UIStyle.styledButton("Remove Selected", UIStyle.DANGER);
        clearCartBtn = UIStyle.styledButton("Clear Cart", Color.GRAY);
        checkoutBtn = UIStyle.styledButton("Checkout", UIStyle.ACCENT);

        buttons.add(removeBtn);
        buttons.add(clearCartBtn);
        buttons.add(checkoutBtn);
        bottom.add(buttons, BorderLayout.SOUTH);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // Package-private getters/accessors
    JTable getMedicineTable() { return medicineTable; }
    JTable getCartTable() { return cartTable; }
    DefaultTableModel getMedicineModel() { return medicineModel; }
    DefaultTableModel getCartModel() { return cartModel; }
    JTextField getSearchField() { return searchField; }
    JTextField getQtyField() { return qtyField; }
    JLabel getTotalLabel() { return totalLabel; }
    JButton getSearchBtn() { return searchBtn; }
    JButton getClearSearchBtn() { return clearSearchBtn; }
    JButton getAddToCartBtn() { return addToCartBtn; }
    JButton getRemoveBtn() { return removeBtn; }
    JButton getClearCartBtn() { return clearCartBtn; }
    JButton getCheckoutBtn() { return checkoutBtn; }
}
