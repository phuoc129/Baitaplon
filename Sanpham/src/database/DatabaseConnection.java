package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.MessageDigest;
import java.util.Base64;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/grocery_store";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";
    
    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Kết nối database thành công!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database!");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Đóng kết nối database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Mã hóa mật khẩu
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }

    // Khởi tạo database đầy đủ
    public static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. Bảng categories
            String createCategoriesTable = "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(100) NOT NULL UNIQUE" +
                    ")";
            stmt.executeUpdate(createCategoriesTable);
            System.out.println("Khởi tạo bảng categories thành công!");
            
            // 2. Bảng products
            String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                    "id VARCHAR(50) PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "category VARCHAR(100) NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "quantity INT NOT NULL" +
                    ")";
            stmt.executeUpdate(createProductsTable);
            System.out.println("Khởi tạo bảng products thành công!");
            
            // 3. Bảng users (tài khoản nhân viên và admin)
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "full_name VARCHAR(100) NOT NULL," +
                    "role VARCHAR(20) NOT NULL," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_login TIMESTAMP NULL" +
                    ")";
            stmt.executeUpdate(createUsersTable);
            System.out.println("Khởi tạo bảng users thành công!");
            
            // 4. Bảng discounts (mã giảm giá)
            String createDiscountsTable = "CREATE TABLE IF NOT EXISTS discounts (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "code VARCHAR(50) NOT NULL UNIQUE," +
                    "percentage DOUBLE NOT NULL," +
                    "start_date DATE NOT NULL," +
                    "end_date DATE NOT NULL," +
                    "active BOOLEAN DEFAULT TRUE" +
                    ")";
            stmt.executeUpdate(createDiscountsTable);
            System.out.println("Khởi tạo bảng discounts thành công!");
            
            // 5. Bảng invoices (hóa đơn)
            String createInvoicesTable = "CREATE TABLE IF NOT EXISTS invoices (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "user_id INT NOT NULL," +
                    "user_name VARCHAR(100)," +
                    "subtotal DOUBLE NOT NULL," +
                    "discount_amount DOUBLE DEFAULT 0," +
                    "total_amount DOUBLE NOT NULL," +
                    "discount_code VARCHAR(50)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")";
            stmt.executeUpdate(createInvoicesTable);
            System.out.println("Khởi tạo bảng invoices thành công!");
            
            // 6. Bảng invoice_details (chi tiết hóa đơn)
            String createInvoiceDetailsTable = "CREATE TABLE IF NOT EXISTS invoice_details (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "invoice_id INT NOT NULL," +
                    "product_id VARCHAR(50) NOT NULL," +
                    "product_name VARCHAR(255) NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "quantity INT NOT NULL," +
                    "subtotal DOUBLE NOT NULL," +
                    "FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE" +
                    ")";
            stmt.executeUpdate(createInvoiceDetailsTable);
            System.out.println("Khởi tạo bảng invoice_details thành công!");
            
            // Thêm dữ liệu mẫu cho categories
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM categories");
            if (rs.next() && rs.getInt("count") == 0) {
                String[] defaultCategories = {
                    "Thực phẩm khô", "Đồ uống", "Rau củ quả",
                    "Sữa và sản phẩm từ sữa", "Gia vị",
                    "Đồ ăn nhanh", "Bánh kẹo", "Đồ dùng cá nhân"
                };
                
                for (String cat : defaultCategories) {
                    stmt.executeUpdate("INSERT INTO categories (name) VALUES ('" + cat + "')");
                }
                System.out.println("Đã thêm các danh mục mặc định!");
            }
            
            // Thêm tài khoản admin mặc định
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next() && rs.getInt("count") == 0) {
                String hashedPassword = hashPassword("admin123");
                stmt.executeUpdate("INSERT INTO users (username, password, full_name, role) " +
                        "VALUES ('admin', '" + hashedPassword + "', 'Administrator', 'ADMIN')");
                System.out.println("Đã tạo tài khoản admin mặc định (user: admin, pass: admin123)");
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo database!");
            e.printStackTrace();
        }
    }
}