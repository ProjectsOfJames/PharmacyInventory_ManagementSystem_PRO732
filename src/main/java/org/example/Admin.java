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

    // Package-private accessors
    ManageMedicinesTab getMedicinesTab() { return medicinesTab; }
    ManageSuppliersTab getSuppliersTab() { return suppliersTab; }
    ManageUsersTab getUsersTab() { return usersTab; }

    //////////////////////////
    // TAB 1: Manage Medicines
    //////////////////////////
    static class ManageMedicinesTab extends JPanel {
        JTable table;
        DefaultTableModel tableModel;
        JTextField nameField, companyField, priceField, qtyField, reorderField, expiryField, searchField;
        JComboBox<String> typeCombo;
        JComboBox<String> supplierCombo; // populated with supplier names in Segment 8
        JButton searchBtn, clearSearchBtn, addBtn, updateBtn, deleteBtn, clearFormBtn;

        ManageMedicinesTab() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(15, 15, 15, 15));
            setBackground(UIStyle.BG_LIGHT);

            add(buildTopPanel(), BorderLayout.NORTH);
            add(buildTablePanel(), BorderLayout.CENTER);
            add(buildFormPanel(), BorderLayout.SOUTH);
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


            buttons.add(addBtn);
            buttons.add(updateBtn);
            buttons.add(deleteBtn);
            buttons.add(clearFormBtn);
            outer.add(buttons, BorderLayout.SOUTH);

            return outer;
        }
    }

    // ==================================================================
    // TAB 2: Manage Suppliers (UI only - CRUD logic added in Segment 9)
    // ==================================================================
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

    // ==================================================================
    // TAB 3: Manage Users (UI only - CRUD logic added in Segment 10)
    // ==================================================================
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
            // TODO (Segment 10): wire up Add/Update/Delete/Clear logic

            buttons.add(addBtn);
            buttons.add(updateBtn);
            buttons.add(deleteBtn);
            buttons.add(clearFormBtn);
            outer.add(buttons, BorderLayout.SOUTH);

            return outer;
        }
    }
}