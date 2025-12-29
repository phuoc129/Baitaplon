package dao;

import database.DatabaseConnection;
import model.Discount;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DiscountDAO {

    // Lấy tất cả mã giảm giá
    public List<Discount> getAllDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        String sql = "SELECT * FROM discounts ORDER BY created_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                discounts.add(extractDiscountFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách mã giảm giá!");
            e.printStackTrace();
        }
        
        return discounts;
    }

    // Tìm mã giảm giá theo code
    public Discount findByCode(String code) {
        String sql = "SELECT * FROM discounts WHERE code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractDiscountFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm mã giảm giá!");
            e.printStackTrace();
        }
        
        return null;
    }

    // Kiểm tra mã giảm giá có hợp lệ không
    public boolean isValidDiscount(String code) {
        Discount discount = findByCode(code);
        return discount != null && discount.isValid();
    }

    // Tính số tiền được giảm
    public double calculateDiscount(String code, double amount) {
        Discount discount = findByCode(code);
        if (discount != null && discount.isValid()) {
            return discount.calculateDiscount(amount);
        }
        return 0;
    }

    // Thêm mã giảm giá mới
    public boolean insertDiscount(Discount discount) {
        String sql = "INSERT INTO discounts (code, percentage, start_date, end_date, active) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, discount.getCode());
            pstmt.setDouble(2, discount.getPercentage());
            pstmt.setDate(3, Date.valueOf(discount.getStartDate()));
            pstmt.setDate(4, Date.valueOf(discount.getEndDate()));
            pstmt.setBoolean(5, discount.isActive());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm mã giảm giá!");
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật mã giảm giá
    public boolean updateDiscount(Discount discount) {
        String sql = "UPDATE discounts SET percentage = ?, start_date = ?, " +
                     "end_date = ?, active = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, discount.getPercentage());
            pstmt.setDate(2, Date.valueOf(discount.getStartDate()));
            pstmt.setDate(3, Date.valueOf(discount.getEndDate()));
            pstmt.setBoolean(4, discount.isActive());
            pstmt.setInt(5, discount.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật mã giảm giá!");
            e.printStackTrace();
            return false;
        }
    }

    // Xóa mã giảm giá
    public boolean deleteDiscount(int id) {
        String sql = "DELETE FROM discounts WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa mã giảm giá!");
            e.printStackTrace();
            return false;
        }
    }

    // Vô hiệu hóa mã giảm giá
    public boolean deactivateDiscount(int id) {
        String sql = "UPDATE discounts SET active = FALSE WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi vô hiệu hóa mã giảm giá!");
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra code đã tồn tại
    public boolean codeExists(String code) {
        String sql = "SELECT COUNT(*) FROM discounts WHERE code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    // Lấy danh sách mã giảm giá còn hiệu lực
    public List<Discount> getActiveDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        String sql = "SELECT * FROM discounts WHERE active = TRUE " +
                     "AND CURRENT_DATE BETWEEN start_date AND end_date " +
                     "ORDER BY percentage DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                discounts.add(extractDiscountFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã giảm giá còn hiệu lực!");
            e.printStackTrace();
        }
        
        return discounts;
    }

    // Helper method
    private Discount extractDiscountFromResultSet(ResultSet rs) throws SQLException {
        Discount discount = new Discount();
        discount.setId(rs.getInt("id"));
        discount.setCode(rs.getString("code"));
        discount.setPercentage(rs.getDouble("percentage"));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            discount.setStartDate(startDate.toLocalDate());
        }
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            discount.setEndDate(endDate.toLocalDate());
        }
        
        discount.setActive(rs.getBoolean("active"));
        
        return discount;
    }
}