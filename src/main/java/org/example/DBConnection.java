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

            try {
                if (conn != null) conn.rollback();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }

            return -1;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}