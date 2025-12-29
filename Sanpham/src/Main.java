import controller.ProductController;
import dao.ProductDAO;
import database.DatabaseConnection;
import model.User;
import view.*;
import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private User currentUser;
    private JTabbedPane tabbedPane;
    private JLabel lblUserInfo;
    
    public Main() {
        // Khởi tạo database
        DatabaseConnection.initDatabase();
        
        // Hiển thị màn hình đăng nhập
        showLogin();
    }

    private void showLogin() {
        LoginDialog loginDialog = new LoginDialog(this);
        loginDialog.setVisible(true);
        
        currentUser = loginDialog.getLoggedInUser();
        
        if (currentUser != null) {
            initComponents();
            setVisible(true);
        } else {
            System.exit(0);
        }
    }

    private void initComponents() {
        setTitle("Hệ thống quản lý cửa hàng tạp hóa - " + currentUser.getFullName());
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content - Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 13));

        // Tab Bán hàng (Phúc)
        SalesPanel salesPanel = new SalesPanel(currentUser);
        tabbedPane.addTab("🛒 Bán hàng", salesPanel);

        // Tab Quản lý sản phẩm (Phước)
        ProductDAO productDAO = new ProductDAO();
        ProductPanel productPanel = new ProductPanel();
        new ProductController(productDAO, productPanel);
        tabbedPane.addTab("📦 Quản lý sản phẩm", productPanel);

        // Tab Doanh thu (Hoàng)
        RevenuePanel revenuePanel = new RevenuePanel();
        tabbedPane.addTab("💰 Doanh thu", revenuePanel);

        // Tab Quản lý tài khoản (chỉ Admin - Hoàng)
        if (currentUser.isAdmin()) {
            UserManagementPanel userPanel = new UserManagementPanel();
            tabbedPane.addTab("👥 Quản lý tài khoản", userPanel);
            
            // Tab Quản lý mã giảm giá (Admin - Thiện)
            DiscountManagementPanel discountPanel = new DiscountManagementPanel();
            tabbedPane.addTab("🎫 Mã giảm giá", discountPanel);
        }

        add(tabbedPane, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(236, 240, 241));
        JLabel lblFooter = new JLabel("© 2024 Nhóm G2 - Hệ thống quản lý cửa hàng tạp hóa");
        lblFooter.setFont(new Font("Arial", Font.ITALIC, 11));
        lblFooter.setForeground(new Color(127, 140, 141));
        footerPanel.add(lblFooter);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left - Title
        JLabel lblTitle = new JLabel("QUẢN LÝ CỬA HÀNG TẠP HÓA");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Right - User info & Logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        rightPanel.setOpaque(false);

        // User info
        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        userInfoPanel.setOpaque(false);
        
        lblUserInfo = new JLabel(currentUser.getFullName());
        lblUserInfo.setFont(new Font("Arial", Font.BOLD, 14));
        lblUserInfo.setForeground(Color.WHITE);
        
        JLabel lblRole = new JLabel(currentUser.getRole());
        lblRole.setFont(new Font("Arial", Font.PLAIN, 11));
        lblRole.setForeground(new Color(236, 240, 241));
        
        userInfoPanel.add(lblUserInfo);
        userInfoPanel.add(lblRole);
        rightPanel.add(userInfoPanel);

        // Logout button
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorderPainted(false);
        btnLogout.setPreferredSize(new Dimension(100, 35));
        
        btnLogout.addActionListener(e -> logout());
        
        rightPanel.add(btnLogout);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            DatabaseConnection.closeConnection();
            
            // Khởi động lại ứng dụng
            SwingUtilities.invokeLater(() -> {
                new Main();
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Main();
        });
    }
}