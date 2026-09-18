package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Admin extends JPanel {

    private final ManageMedicinesTab medicinesTab;
    private final ManageSuppliersTab suppliersTab;
    private final ManageUsersTab usersTab;

    public Admin() {
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIStyle.FONT_LABEL);

        medicinesTab = new ManageMedicinesTab();
        suppliersTab = new ManageSuppliersTab();
        usersTab = new ManageUsersTab();

        tabs.addTab("Manage Medicines", medicinesTab);
        tabs.addTab("Manage Suppliers", suppliersTab);
        tabs.addTab("Manage Users", usersTab);

        add(tabs, BorderLayout.CENTER);
    }

    // Package-private accessors so later segments can attach logic
    ManageMedicinesTab getMedicinesTab() { return medicinesTab; }
    ManageSuppliersTab getSuppliersTab() { return suppliersTab; }
    ManageUsersTab getUsersTab() { return usersTab; }

    // TAB 1: Manage Medicines
    static class ManageMedicinesTab extends JPanel {
        JTable table;
        DefaultTableModel tableModel;
        JTextField nameField, companyField, priceField, qtyField, reorderField, expiryField, searchField;
        JComboBox<String> typeCombo;
        JComboBox<Models.Supplier> supplierCombo;
        JButton searchBtn, clearSearchBtn, addBtn, updateBtn, deleteBtn, clearFormBtn;
        int selectedMedicineId = -1;

        ManageMedicinesTab() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(15, 15, 15, 15));
            setBackground(UIStyle.BG_LIGHT);

            add(buildTopPanel(), BorderLayout.NORTH);
            add(buildTablePanel(), BorderLayout.CENTER);
            add(buildFormPanel(), BorderLayout.SOUTH);

            loadMedicines(null);
        }

        private JPanel buildTopPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.add(UIStyle.headerLabel("Manage Medicines"), BorderLayout.WEST);

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

        private JScrollPane buildTablePanel() {
            String[] cols = {"ID", "Name", "Company", "Type", "Price (R)", "Stock", "Reorder Lvl", "Expiry", "Supplier"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
            table = new JTable(tableModel);
            UIStyle.styleTable(table);
            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) populateFormFromRow(table.getSelectedRow());
            });
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(1000, 280));
            return scroll;
        }

        private JPanel buildFormPanel() {
            JPanel outer = new JPanel(new BorderLayout());
            outer.setOpaque(false);

            JPanel grid = new JPanel(new GridLayout(2, 4, 10, 10));
            grid.setBorder(BorderFactory.createTitledBorder("Medicine Details"));
            grid.setBackground(UIStyle.BG_LIGHT);

            nameField = new JTextField();
            companyField = new JTextField();
            typeCombo = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream"});
            priceField = new JTextField();
            qtyField = new JTextField();
            reorderField = new JTextField();
            expiryField = new JTextField(); // format yyyy-MM-dd
            supplierCombo = new JComboBox<>();
            refreshSupplierCombo();

            grid.add(UIStyle.labeledField("Name", nameField));
            grid.add(UIStyle.labeledField("Company", companyField));
            grid.add(UIStyle.labeledField("Type", typeCombo));
            grid.add(UIStyle.labeledField("Price (R)", priceField));
            grid.add(UIStyle.labeledField("Stock Qty", qtyField));
            grid.add(UIStyle.labeledField("Reorder Level", reorderField));
            grid.add(UIStyle.labeledField("Expiry (yyyy-MM-dd)", expiryField));
            grid.add(UIStyle.labeledField("Supplier", supplierCombo));

            outer.add(grid, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            buttons.setOpaque(false);
            addBtn = UIStyle.styledButton("Add", UIStyle.SUCCESS);
            updateBtn = UIStyle.styledButton("Update", UIStyle.PRIMARY);
            deleteBtn = UIStyle.styledButton("Delete", UIStyle.DANGER);
            clearFormBtn = UIStyle.styledButton("Clear Form", Color.GRAY);
            addBtn.addActionListener(e -> addMedicine());
            updateBtn.addActionListener(e -> updateMedicine());
            deleteBtn.addActionListener(e -> deleteMedicine());
            clearFormBtn.addActionListener(e -> clearForm());

            buttons.add(addBtn);
            buttons.add(updateBtn);
            buttons.add(deleteBtn);
            buttons.add(clearFormBtn);
            outer.add(buttons, BorderLayout.SOUTH);

            return outer;
        }

        private void refreshSupplierCombo() {
            supplierCombo.removeAllItems();
            for (Models.Supplier s : DBConnection.getAllSuppliers()) {
                supplierCombo.addItem(s);
            }
        }

        private void loadMedicines(String keyword) {
            java.util.List<Models.Medicine> meds = (keyword == null) ? DBConnection.getAllMedicines() : DBConnection.searchMedicines(keyword);
            tableModel.setRowCount(0);
            for (Models.Medicine m : meds) {
                tableModel.addRow(new Object[]{
                        m.getMedicineId(), m.getName(), m.getCompany(), m.getMedicineType(),
                        m.getPrice(), m.getQuantityInStock(), m.getReorderLevel(),
                        m.getExpiryDate(), m.getSupplierName()
                });
            }
        }

        private void populateFormFromRow(int row) {
            selectedMedicineId = (int) tableModel.getValueAt(row, 0);
            nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
            companyField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
            typeCombo.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
            priceField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
            qtyField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
            reorderField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
            expiryField.setText(String.valueOf(tableModel.getValueAt(row, 7)));
            String supplierName = String.valueOf(tableModel.getValueAt(row, 8));
            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                if (supplierCombo.getItemAt(i).getName().equals(supplierName)) {
                    supplierCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        private boolean validateForm() {
            if (nameField.getText().trim().isEmpty() || priceField.getText().trim().isEmpty()
                    || qtyField.getText().trim().isEmpty() || reorderField.getText().trim().isEmpty()
                    || expiryField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            try {
                new java.math.BigDecimal(priceField.getText().trim());
                Integer.parseInt(qtyField.getText().trim());
                Integer.parseInt(reorderField.getText().trim());
                java.sql.Date.valueOf(expiryField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid number or date format. Use yyyy-MM-dd for date.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

        private Models.Medicine buildMedicineFromForm() {
            Models.Medicine m = new Models.Medicine();
            m.setMedicineId(selectedMedicineId);
            m.setName(nameField.getText().trim());
            m.setCompany(companyField.getText().trim());
            m.setMedicineType((String) typeCombo.getSelectedItem());
            m.setPrice(new java.math.BigDecimal(priceField.getText().trim()));
            m.setQuantityInStock(Integer.parseInt(qtyField.getText().trim()));
            m.setReorderLevel(Integer.parseInt(reorderField.getText().trim()));
            m.setExpiryDate(java.sql.Date.valueOf(expiryField.getText().trim()));
            Models.Supplier sel = (Models.Supplier) supplierCombo.getSelectedItem();
            m.setSupplierId(sel != null ? sel.getSupplierId() : 0);
            return m;
        }

        private void addMedicine() {
            if (!validateForm()) return;
            if (DBConnection.addMedicine(buildMedicineFromForm())) {
                JOptionPane.showMessageDialog(this, "Medicine added successfully.");
                clearForm();
                loadMedicines(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add medicine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void updateMedicine() {
            if (selectedMedicineId == -1) {
                JOptionPane.showMessageDialog(this, "Select a medicine from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!validateForm()) return;
            if (DBConnection.updateMedicine(buildMedicineFromForm())) {
                JOptionPane.showMessageDialog(this, "Medicine updated successfully.");
                clearForm();
                loadMedicines(null);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update medicine.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteMedicine() {
            if (selectedMedicineId == -1) {
                JOptionPane.showMessageDialog(this, "Select a medicine from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this medicine permanently?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (DBConnection.deleteMedicine(selectedMedicineId)) {
                    JOptionPane.showMessageDialog(this, "Medicine deleted.");
                    clearForm();
                    loadMedicines(null);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete (it may be referenced by past sales).",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private void clearForm() {
            selectedMedicineId = -1;
            nameField.setText("");
            companyField.setText("");
            typeCombo.setSelectedIndex(0);
            priceField.setText("");
            qtyField.setText("");
            reorderField.setText("");
            expiryField.setText("");
            refreshSupplierCombo();
            table.clearSelection();
        }
    }

    // TAB 2: Manage Suppliers
    static class ManageSuppliersTab extends JPanel {
        JTable table;
        DefaultTableModel tableModel;
        JTextField nameField, contactField, phoneField, emailField, addressField;
        JButton addBtn, updateBtn, deleteBtn, clearFormBtn;

        ManageSuppliersTab() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(15, 15, 15, 15));
            setBackground(UIStyle.BG_LIGHT);

            add(UIStyle.headerLabel("Manage Suppliers"), BorderLayout.NORTH);
            add(buildTablePanel(), BorderLayout.CENTER);
            add(buildFormPanel(), BorderLayout.SOUTH);
        }

        private JScrollPane buildTablePanel() {
            String[] cols = {"ID", "Name", "Contact Person", "Phone", "Email", "Address"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
            table = new JTable(tableModel);
            UIStyle.styleTable(table);
            // TODO (Segment 9): wire up row-selection -> populate form logic
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(1000, 320));
            return scroll;
        }

        private JPanel buildFormPanel() {
            JPanel outer = new JPanel(new BorderLayout());
            outer.setOpaque(false);

            JPanel grid = new JPanel(new GridLayout(1, 5, 10, 10));
            grid.setBorder(BorderFactory.createTitledBorder("Supplier Details"));
            grid.setBackground(UIStyle.BG_LIGHT);

            nameField = new JTextField();
            contactField = new JTextField();
            phoneField = new JTextField();
            emailField = new JTextField();
            addressField = new JTextField();

            grid.add(UIStyle.labeledField("Name", nameField));
            grid.add(UIStyle.labeledField("Contact Person", contactField));
            grid.add(UIStyle.labeledField("Phone", phoneField));
            grid.add(UIStyle.labeledField("Email", emailField));
            grid.add(UIStyle.labeledField("Address", addressField));

            outer.add(grid, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            buttons.setOpaque(false);
            addBtn = UIStyle.styledButton("Add", UIStyle.SUCCESS);
            updateBtn = UIStyle.styledButton("Update", UIStyle.PRIMARY);
            deleteBtn = UIStyle.styledButton("Delete", UIStyle.DANGER);
            clearFormBtn = UIStyle.styledButton("Clear Form", Color.GRAY);
            // TODO (Segment 9): wire up Add/Update/Delete/Clear logic

            buttons.add(addBtn);
            buttons.add(updateBtn);
            buttons.add(deleteBtn);
            buttons.add(clearFormBtn);
            outer.add(buttons, BorderLayout.SOUTH);

            return outer;
        }
    }


    // TAB 3: Manage Users
    static class ManageUsersTab extends JPanel {
        JTable table;
        DefaultTableModel tableModel;
        JTextField usernameField, fullNameField;
        JPasswordField passwordField;
        JComboBox<String> roleCombo;
        JButton addBtn, updateBtn, deleteBtn, clearFormBtn;

        ManageUsersTab() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(15, 15, 15, 15));
            setBackground(UIStyle.BG_LIGHT);

            add(UIStyle.headerLabel("Manage Users"), BorderLayout.NORTH);
            add(buildTablePanel(), BorderLayout.CENTER);
            add(buildFormPanel(), BorderLayout.SOUTH);
        }

        private JScrollPane buildTablePanel() {
            String[] cols = {"ID", "Username", "Full Name", "Role"};
            tableModel = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
            table = new JTable(tableModel);
            UIStyle.styleTable(table);
            // TODO (Segment 10): wire up row-selection -> populate form logic
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(1000, 320));
            return scroll;
        }

        private JPanel buildFormPanel() {
            JPanel outer = new JPanel(new BorderLayout());
            outer.setOpaque(false);

            JPanel grid = new JPanel(new GridLayout(1, 4, 10, 10));
            grid.setBorder(BorderFactory.createTitledBorder("User Details"));
            grid.setBackground(UIStyle.BG_LIGHT);

            usernameField = new JTextField();
            passwordField = new JPasswordField();
            fullNameField = new JTextField();
            roleCombo = new JComboBox<>(new String[]{"Cashier", "Admin"});

            grid.add(UIStyle.labeledField("Username", usernameField));
            grid.add(UIStyle.labeledField("Password", passwordField));
            grid.add(UIStyle.labeledField("Full Name", fullNameField));
            grid.add(UIStyle.labeledField("Role", roleCombo));

            outer.add(grid, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            buttons.setOpaque(false);
            addBtn = UIStyle.styledButton("Add User", UIStyle.SUCCESS);
            updateBtn = UIStyle.styledButton("Update", UIStyle.PRIMARY);
            deleteBtn = UIStyle.styledButton("Delete", UIStyle.DANGER);
            clearFormBtn = UIStyle.styledButton("Clear Form", Color.GRAY);

            buttons.add(addBtn);
            buttons.add(updateBtn);
            buttons.add(deleteBtn);
            buttons.add(clearFormBtn);
            outer.add(buttons, BorderLayout.SOUTH);

            return outer;
        }
    }
}