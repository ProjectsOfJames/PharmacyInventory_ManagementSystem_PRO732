package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PointOfSales extends JPanel {

    private final Models.User cashier;
    private final List<Models.SaleItem> cart = new ArrayList<>();
    private final List<Models.Medicine> loadedMedicines = new ArrayList<>();

    private JTable medicineTable, cartTable;
    private DefaultTableModel medicineModel, cartModel;
    private JTextField searchField, qtyField;
    private JLabel totalLabel;
    private JButton searchBtn, clearSearchBtn, addToCartBtn, removeBtn, clearCartBtn, checkoutBtn;

    public PointOfSales(Models.User cashier) {
        this.cashier = cashier;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(UIStyle.BG_LIGHT);

        add(buildTopSearchPanel(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildMedicineListPanel(), buildCartPanel());
        split.setResizeWeight(0.55);
        add(split, BorderLayout.CENTER);

        loadMedicines(null);
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
        searchBtn.addActionListener(e -> {
            String kw = searchField.getText().trim();
            loadMedicines(kw.isEmpty() ? null : kw);
        });
        clearSearchBtn.addActionListener(e -> { searchField.setText(""); loadMedicines(null); });

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearSearchBtn);
        panel.add(searchPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildMedicineListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
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
        addToCartBtn.addActionListener(e -> addToCart());
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
        removeBtn.addActionListener(e -> removeSelectedFromCart());
        checkoutBtn.addActionListener(e -> checkout());

        buttons.add(removeBtn);
        buttons.add(clearCartBtn);
        buttons.add(checkoutBtn);
        bottom.add(buttons, BorderLayout.SOUTH);

        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void loadMedicines(String keyword) {
        loadedMedicines.clear();
        loadedMedicines.addAll(keyword == null ? DBConnection.getAllMedicines() : DBConnection.searchMedicines(keyword));
        medicineModel.setRowCount(0);
        for (Models.Medicine m : loadedMedicines) {
            medicineModel.addRow(new Object[]{m.getName(), m.getCompany(), m.getPrice(), m.getQuantityInStock()});
        }
    }

    private void addToCart() {
        int row = medicineTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a medicine first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity (whole number > 0).", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Models.Medicine selected = loadedMedicines.get(row);
        if (qty > selected.getQuantityInStock()) {
            JOptionPane.showMessageDialog(this, "Insufficient stock. Only " + selected.getQuantityInStock() + " available.",
                    "Stock Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // If already in cart, increase quantity instead of adding a duplicate row
        for (Models.SaleItem item : cart) {
            if (item.getMedicineId() == selected.getMedicineId()) {
                int newQty = item.getQuantitySold() + qty;
                if (newQty > selected.getQuantityInStock()) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock for combined quantity.", "Stock Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantitySold(newQty);
                refreshCartTable();
                return;
            }
        }

        cart.add(new Models.SaleItem(selected.getMedicineId(), selected.getName(), qty, selected.getPrice()));
        refreshCartTable();
    }

    private void removeSelectedFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a cart item to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cart.remove(row);
        refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (Models.SaleItem item : cart) {
            cartModel.addRow(new Object[]{
                    item.getMedicineName(), item.getQuantitySold(), item.getPriceAtSale(), item.getLineTotal()
            });
            total = total.add(item.getLineTotal());
        }
        totalLabel.setText(String.format("Total: R%.2f", total));
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.", "Nothing to Checkout", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal total = cart.stream().map(Models.SaleItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Confirm sale for R%.2f (%d item(s))?", total, cart.size()),
                "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int saleId = DBConnection.processSale(cashier.getUserId(), cart, total);
        if (saleId == -1) {
            JOptionPane.showMessageDialog(this, "Checkout failed - stock may have changed. Please review cart.",
                    "Checkout Error", JOptionPane.ERROR_MESSAGE);
            loadMedicines(null);
            return;
        }

        JOptionPane.showMessageDialog(this, "Sale #" + saleId + " completed successfully. Total: R" + total,
                "Checkout Successful", JOptionPane.INFORMATION_MESSAGE);

        cart.clear();
        refreshCartTable();
        loadMedicines(null);
    }

    // Package-private getters/accessors so later segments can attach logic without altering UI structure
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
    List<Models.SaleItem> getCart() { return cart; }
}
