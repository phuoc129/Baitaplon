package dao;

import database.DatabaseConnection;
import model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAO {

    // Lấy tất cả danh mục
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("id"),
                    rs.getString("name")
                );
                categories.add(category);
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách danh mục!");
            e.printStackTrace();
        }
        
        return categories;
    }

    // Thêm danh mục mới
    public boolean insertCategory(Category category) {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getName());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm danh mục!");
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật danh mục
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setInt(2, category.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật danh mục!");
            e.printStackTrace();
            return false;
        }
    }

    // Xóa danh mục
    public boolean deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa danh mục!");
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra danh mục đã tồn tại
    public boolean categoryExists(String name) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra danh mục!");
            e.printStackTrace();
        }
        
        return false;
    }

    // THÊM MỚI: Thống kê số lượng sản phẩm theo danh mục (cho Phước)
    public Map<String, Integer> getProductCountByCategory() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT category, COUNT(*) as count FROM products GROUP BY category";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("category"), rs.getInt("count"));
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thống kê sản phẩm theo danh mục!");
            e.printStackTrace();
        }
        
        return stats;
    }
}