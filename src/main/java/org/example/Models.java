package org.example;

public class Models {

    public static class User {
        private int userId;
        private String username;
        private String password;
        private String role; // "Admin" | "Cashier"
        private String fullName;

        public User() {}

        public User(int userId, String username, String password, String role, String fullName) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.role = role;
            this.fullName = fullName;
        }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        @Override
        public String toString() { return username + " (" + role + ")"; }
    }

    public static class Medicine {
        private int medicineId;
        private String name;
        private String company;
        private String medicineType;
        private java.math.BigDecimal price;
        private int quantityInStock;
        private int reorderLevel;
        private java.sql.Date expiryDate;
        private int supplierId;
        private String supplierName; // convenience field for display

        public Medicine() {}

        public Medicine(int medicineId, String name, String company, String medicineType, java.math.BigDecimal price,
                        int quantityInStock, int reorderLevel, java.sql.Date expiryDate, int supplierId) {
            this.medicineId = medicineId;
            this.name = name;
            this.company = company;
            this.medicineType = medicineType;
            this.price = price;
            this.quantityInStock = quantityInStock;
            this.reorderLevel = reorderLevel;
            this.expiryDate = expiryDate;
            this.supplierId = supplierId;
        }

        public int getMedicineId() { return medicineId; }
        public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getMedicineType() { return medicineType; }
        public void setMedicineType(String medicineType) { this.medicineType = medicineType; }

        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }

        public int getQuantityInStock() { return quantityInStock; }
        public void setQuantityInStock(int quantityInStock) { this.quantityInStock = quantityInStock; }

        public int getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

        public java.sql.Date getExpiryDate() { return expiryDate; }
        public void setExpiryDate(java.sql.Date expiryDate) { this.expiryDate = expiryDate; }

        public int getSupplierId() { return supplierId; }
        public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

        public boolean isLowStock() { return quantityInStock <= reorderLevel; }

        @Override
        public String toString() { return name; }
    }

    public static class SaleItem {
        private int medicineId;
        private String medicineName;
        private int quantitySold;
        private java.math.BigDecimal priceAtSale;

        public SaleItem() {}

        public SaleItem(int medicineId, String medicineName, int quantitySold, java.math.BigDecimal priceAtSale) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.quantitySold = quantitySold;
            this.priceAtSale = priceAtSale;
        }

        public int getMedicineId() { return medicineId; }
        public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public int getQuantitySold() { return quantitySold; }
        public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }

        public java.math.BigDecimal getPriceAtSale() { return priceAtSale; }
        public void setPriceAtSale(java.math.BigDecimal priceAtSale) { this.priceAtSale = priceAtSale; }

        public java.math.BigDecimal getLineTotal() {
            return priceAtSale.multiply(java.math.BigDecimal.valueOf(quantitySold));
        }
    }
}