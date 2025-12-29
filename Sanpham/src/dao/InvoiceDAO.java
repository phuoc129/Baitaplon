package dao;

import database.DatabaseConnection;
import model.Invoice;
import model.InvoiceDetail;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceDAO {

    // Lưu hóa đơn và chi tiết (Transaction)
    public boolean saveInvoice(Invoice invoice) {
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction
            
            // 1. Lưu hóa đơn
            String invoiceSql = "INSERT INTO invoices (user_id, user_name, subtotal, " +
                               "discount_amount, total_amount, discount_code) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement invoiceStmt = conn.prepareStatement(invoiceSql, Statement.RETURN_GENERATED_KEYS);
            invoiceStmt.setInt(1, invoice.getUserId());
            invoiceStmt.setString(2, invoice.getUserName());
            invoiceStmt.setDouble(3, invoice.getSubtotal());
            invoiceStmt.setDouble(4, invoice.getDiscountAmount());
            invoiceStmt.setDouble(5, invoice.getTotalAmount());
            invoiceStmt.setString(6, invoice.getDiscountCode());
            
            int rowsAffected = invoiceStmt.executeUpdate();
            if (rowsAffected == 0) {
                conn.rollback();
                return false;
            }
            
            // Lấy ID hóa đơn vừa tạo
            ResultSet generatedKeys = invoiceStmt.getGeneratedKeys();
            int invoiceId = 0;
            if (generatedKeys.next()) {
                invoiceId = generatedKeys.getInt(1);
            }
            
            // 2. Lưu chi tiết hóa đơn
            String detailSql = "INSERT INTO invoice_details (invoice_id, product_id, " +
                              "product_name, price, quantity, subtotal) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement detailStmt = conn.prepareStatement(detailSql);
            
            for (InvoiceDetail detail : invoice.getDetails()) {
                detailStmt.setInt(1, invoiceId);
                detailStmt.setString(2, detail.getProductId());
                detailStmt.setString(3, detail.getProductName());
                detailStmt.setDouble(4, detail.getPrice());
                detailStmt.setInt(5, detail.getQuantity());
                detailStmt.setDouble(6, detail.getSubtotal());
                detailStmt.addBatch();
            }
            
            detailStmt.executeBatch();
            
            // 3. Cập nhật số lượng sản phẩm trong kho
            String updateProductSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateProductSql);
            
            for (InvoiceDetail detail : invoice.getDetails()) {
                updateStmt.setInt(1, detail.getQuantity());
                updateStmt.setString(2, detail.getProductId());
                updateStmt.addBatch();
            }
            
            updateStmt.executeBatch();
            
            conn.commit(); // Hoàn thành transaction
            return true;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu hóa đơn!");
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Lấy tất cả hóa đơn
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices ORDER BY created_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setDetails(getInvoiceDetails(invoice.getId()));
                invoices.add(invoice);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn!");
            e.printStackTrace();
        }
        
        return invoices;
    }

    // Lấy chi tiết hóa đơn
    public List<InvoiceDetail> getInvoiceDetails(int invoiceId) {
        List<InvoiceDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM invoice_details WHERE invoice_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                InvoiceDetail detail = new InvoiceDetail();
                detail.setId(rs.getInt("id"));
                detail.setInvoiceId(rs.getInt("invoice_id"));
                detail.setProductId(rs.getString("product_id"));
                detail.setProductName(rs.getString("product_name"));
                detail.setPrice(rs.getDouble("price"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setSubtotal(rs.getDouble("subtotal"));
                details.add(detail);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn!");
            e.printStackTrace();
        }
        
        return details;
    }

    // Tìm hóa đơn theo ID
    public Invoice findById(int id) {
        String sql = "SELECT * FROM invoices WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setDetails(getInvoiceDetails(id));
                return invoice;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm hóa đơn!");
            e.printStackTrace();
        }
        
        return null;
    }

    // Lấy hóa đơn theo khoảng thời gian
    public List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE DATE(created_date) BETWEEN ? AND ? " +
                     "ORDER BY created_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Invoice invoice = extractInvoiceFromResultSet(rs);
                invoice.setDetails(getInvoiceDetails(invoice.getId()));
                invoices.add(invoice);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy hóa đơn theo thời gian!");
            e.printStackTrace();
        }
        
        return invoices;
    }

    // Tính tổng doanh thu theo ngày
    public double getRevenueByDate(LocalDate date) {
        String sql = "SELECT SUM(total_amount) as revenue FROM invoices " +
                     "WHERE DATE(created_date) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("revenue");
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính doanh thu!");
            e.printStackTrace();
        }
        
        return 0;
    }

    // Tính tổng doanh thu theo tháng
    public double getRevenueByMonth(int year, int month) {
        String sql = "SELECT SUM(total_amount) as revenue FROM invoices " +
                     "WHERE YEAR(created_date) = ? AND MONTH(created_date) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("revenue");
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính doanh thu tháng!");
            e.printStackTrace();
        }
        
        return 0;
    }

    // Thống kê doanh thu theo ngày trong khoảng thời gian
    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Double> revenueMap = new HashMap<>();
        String sql = "SELECT DATE(created_date) as date, SUM(total_amount) as revenue " +
                     "FROM invoices WHERE DATE(created_date) BETWEEN ? AND ? " +
                     "GROUP BY DATE(created_date) ORDER BY date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                double revenue = rs.getDouble("revenue");
                revenueMap.put(date, revenue);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thống kê doanh thu theo ngày!");
            e.printStackTrace();
        }
        
        return revenueMap;
    }

    // Đếm số hóa đơn
    public int getInvoiceCount() {
        String sql = "SELECT COUNT(*) as total FROM invoices";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm hóa đơn!");
            e.printStackTrace();
        }
        
        return 0;
    }

    // Helper method
    private Invoice extractInvoiceFromResultSet(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("id"));
        
        Timestamp created = rs.getTimestamp("created_date");
        if (created != null) {
            invoice.setCreatedDate(created.toLocalDateTime());
        }
        
        invoice.setUserId(rs.getInt("user_id"));
        invoice.setUserName(rs.getString("user_name"));
        invoice.setSubtotal(rs.getDouble("subtotal"));
        invoice.setDiscountAmount(rs.getDouble("discount_amount"));
        invoice.setTotalAmount(rs.getDouble("total_amount"));
        invoice.setDiscountCode(rs.getString("discount_code"));
        
        return invoice;
    }
}