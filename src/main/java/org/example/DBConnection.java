package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv("DBurl");
    private static final String USER = System.getenv("DBuser");
    private static final String PASSWORD = System.getenv("DBpassword");

    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC Driver not found.", e);
            }
        }
        return connection;
    }

    // Authenticates a user against the users table
    public static Models.User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Models.User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("full_name")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Registers a new user
    public static boolean registerUser(String username, String password, String role, String fullName) {
        String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.setString(4, fullName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final String MEDICINE_SELECT =
            "SELECT m.*, s.name AS supplier_name FROM medicines m " +
            "LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id ";

    // Returns all medicines, ordered by ID
    public static java.util.List<Models.Medicine> getAllMedicines() {
        java.util.List<Models.Medicine> list = new java.util.ArrayList<>();
        String sql = MEDICINE_SELECT + "ORDER BY m.medicine_id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMedicineRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Returns medicines whose name or company matches the keyword
    public static java.util.List<Models.Medicine> searchMedicines(String keyword) {
        java.util.List<Models.Medicine> list = new java.util.ArrayList<>();
        String sql = MEDICINE_SELECT + "WHERE m.name ILIKE ? OR m.company ILIKE ? ORDER BY m.name";

        try (
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        )
        {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMedicineRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Models.Medicine mapMedicineRow(ResultSet rs) throws SQLException {
        Models.Medicine m = new Models.Medicine(
                rs.getInt("medicine_id"),
                rs.getString("name"),
                rs.getString("company"),
                rs.getString("medicine_type"),
                rs.getBigDecimal("price"),
                rs.getInt("quantity_in_stock"),
                rs.getInt("reorder_level"),
                rs.getDate("expiry_date"),
                rs.getInt("supplier_id")
        );
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }

    /*
     * Processes a full sale transaction (sale header, each sale_item, decrements stock) for each medicine
     * Rolls back if any item has insufficient stock
     * Returns the new sale_id, or -1 on failure
     */
    public static int processSale(int cashierUserId, java.util.List<Models.SaleItem> items, java.math.BigDecimal totalAmount) {
        String insertSale = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?) RETURNING sale_id";
        String checkStock = "SELECT quantity_in_stock FROM medicines WHERE medicine_id = ? FOR UPDATE";
        String updateStock = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ?";
        String insertItem = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int saleId;
            try (PreparedStatement ps = conn.prepareStatement(insertSale)) {
                ps.setBigDecimal(1, totalAmount);
                ps.setInt(2, cashierUserId);
                try (ResultSet keys = ps.executeQuery()) {
                    if (!keys.next()) throw new SQLException("Failed to obtain sale_id");
                    saleId = keys.getInt(1);
                }
            }

            for (Models.SaleItem item : items) {
                try (PreparedStatement ps = conn.prepareStatement(checkStock)) {
                    ps.setInt(1, item.getMedicineId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || rs.getInt("quantity_in_stock") < item.getQuantitySold()) {
                            conn.rollback();
                            return -1; // insufficient stock
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(updateStock)) {
                    ps.setInt(1, item.getQuantitySold());
                    ps.setInt(2, item.getMedicineId());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    ps.setInt(1, saleId);
                    ps.setInt(2, item.getMedicineId());
                    ps.setInt(3, item.getQuantitySold());
                    ps.setBigDecimal(4, item.getPriceAtSale());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return saleId;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Returns all suppliers, ordered by ID (used to populate the supplier dropdown)
    public static java.util.List<Models.Supplier> getAllSuppliers() {
        java.util.List<Models.Supplier> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY supplier_id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapSupplierRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static Models.Supplier mapSupplierRow(ResultSet rs) throws SQLException {
        return new Models.Supplier(
                rs.getInt("supplier_id"),
                rs.getString("name"),
                rs.getString("contact_person"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address")
        );
    }

    // Inserts a new supplier
    public static boolean addSupplier(Models.Supplier s) {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSupplier(ps, s);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates an existing supplier by supplier_id
    public static boolean updateSupplier(Models.Supplier s) {
        String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindSupplier(ps, s);
            ps.setInt(6, s.getSupplierId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deletes a supplier by ID. Returns false if it fails
    public static boolean deleteSupplier(int supplierId) {
        String sql = "DELETE FROM suppliers WHERE supplier_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void bindSupplier(PreparedStatement ps, Models.Supplier s) throws SQLException {
        ps.setString(1, s.getName());
        ps.setString(2, s.getContactPerson());
        ps.setString(3, s.getPhone());
        ps.setString(4, s.getEmail());
        ps.setString(5, s.getAddress());
    }

    // Returns all users, ordered by ID
    public static java.util.List<Models.User> getAllUsers() {
        java.util.List<Models.User> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Models.User(
                        rs.getInt("user_id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("role"), rs.getString("full_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Updates an existing user by user_id
    public static boolean updateUser(Models.User u) {
        String sql = "UPDATE users SET username=?, password=?, role=?, full_name=? WHERE user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getFullName());
            ps.setInt(5, u.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deletes a user by ID. Returns false if it fails
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Simple holder for the sales summary (total transactions + total revenue) over a date range
    public static class SalesSummary {
        public final int numSales;
        public final java.math.BigDecimal revenue;
        public SalesSummary(int numSales, java.math.BigDecimal revenue) {
            this.numSales = numSales;
            this.revenue = revenue;
        }
    }

    // Sales report: one row per transaction, between two dates (inclusive)
    public static javax.swing.table.DefaultTableModel getSalesReport(java.sql.Date from, java.sql.Date to) {
        String[] cols = {"Sale ID", "Date/Time", "Cashier", "Total (R)"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0);
        String sql = "SELECT s.sale_id, s.sale_date, u.full_name, s.total_amount " +
                "FROM sales s LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE s.sale_date::date BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("sale_id"), rs.getTimestamp("sale_date"),
                            rs.getString("full_name"), rs.getBigDecimal("total_amount")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    // Total transactions and total revenue between two dates (inclusive)
    public static SalesSummary getSalesSummary(java.sql.Date from, java.sql.Date to) {
        String sql = "SELECT COUNT(*) AS num_sales, COALESCE(SUM(total_amount),0) AS revenue " +
                "FROM sales WHERE sale_date::date BETWEEN ? AND ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SalesSummary(rs.getInt("num_sales"), rs.getBigDecimal("revenue"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new SalesSummary(0, java.math.BigDecimal.ZERO);
    }

    // Item-wise report: total quantity and revenue per medicine, between two dates
    public static javax.swing.table.DefaultTableModel getItemWiseReport(java.sql.Date from, java.sql.Date to) {
        String[] cols = {"Medicine", "Qty Sold", "Revenue (R)"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0);
        String sql = "SELECT m.name, SUM(si.quantity_sold) AS qty, SUM(si.quantity_sold * si.price_at_sale) AS revenue " +
                "FROM sale_items si " +
                "JOIN sales s ON si.sale_id = s.sale_id " +
                "JOIN medicines m ON si.medicine_id = m.medicine_id " +
                "WHERE s.sale_date::date BETWEEN ? AND ? " +
                "GROUP BY m.medicine_id, m.name ORDER BY qty DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("name"), rs.getInt("qty"), rs.getBigDecimal("revenue")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    // Low stock report: medicines at or below their reorder level
    public static javax.swing.table.DefaultTableModel getLowStockReport() {
        String[] cols = {"Medicine", "Company", "In Stock", "Reorder Level", "Supplier"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0);
        String sql = "SELECT m.name, m.company, m.quantity_in_stock, m.reorder_level, s.name AS supplier_name " +
                "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "WHERE m.quantity_in_stock <= m.reorder_level ORDER BY m.quantity_in_stock ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"), rs.getString("company"),
                        rs.getInt("quantity_in_stock"), rs.getInt("reorder_level"),
                        rs.getString("supplier_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    // Expiry report: medicines expiring within the next N days
    public static javax.swing.table.DefaultTableModel getExpiryReport(int withinDays) {
        String[] cols = {"Medicine", "Company", "Expiry Date", "Qty In Stock", "Supplier"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0);
        String sql = "SELECT m.name, m.company, m.expiry_date, m.quantity_in_stock, s.name AS supplier_name " +
                "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id " +
                "WHERE m.expiry_date <= (CURRENT_DATE + (? || ' days')::interval) " +
                "ORDER BY m.expiry_date ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, withinDays);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("name"), rs.getString("company"),
                            rs.getDate("expiry_date"), rs.getInt("quantity_in_stock"),
                            rs.getString("supplier_name")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    // Inserts a new medicine. supplierId may be 0/negative to store NULL
    public static boolean addMedicine(Models.Medicine m) {
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindMedicine(ps, m);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates an existing medicine by medicine_id
    public static boolean updateMedicine(Models.Medicine m) {
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, " +
                "reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindMedicine(ps, m);
            ps.setInt(9, m.getMedicineId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deletes a medicine by ID. Returns false if it fails (e.g. referenced by past sales)
    public static boolean deleteMedicine(int medicineId) {
        String sql = "DELETE FROM medicines WHERE medicine_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void bindMedicine(PreparedStatement ps, Models.Medicine m) throws SQLException {
        ps.setString(1, m.getName());
        ps.setString(2, m.getCompany());
        ps.setString(3, m.getMedicineType());
        ps.setBigDecimal(4, m.getPrice());
        ps.setInt(5, m.getQuantityInStock());
        ps.setInt(6, m.getReorderLevel());
        ps.setDate(7, m.getExpiryDate());
        if (m.getSupplierId() > 0) {
            ps.setInt(8, m.getSupplierId());
        } else {
            ps.setNull(8, java.sql.Types.INTEGER);
        }
    }
}