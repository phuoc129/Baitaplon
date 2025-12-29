package controller;

import dao.CategoryDAO;
import dao.ProductDAO;
import model.Category;
import model.Product;
import view.CategoryDialog;
import view.ProductPanel;
import javax.swing.*;
import java.awt.event.*;

public class ProductController {
    private ProductDAO productDAO;
    private CategoryDAO categoryDAO;
    private ProductPanel productPanel;

    public ProductController(ProductDAO productDAO, ProductPanel productPanel) {
        this.productDAO = productDAO;
        this.categoryDAO = new CategoryDAO();
        this.productPanel = productPanel;
        
        initController();
        loadCategories();
        loadProducts();
    }

    private void initController() {
        productPanel.getBtnAdd().addActionListener(e -> addProduct());
        productPanel.getBtnUpdate().addActionListener(e -> updateProduct());
        productPanel.getBtnDelete().addActionListener(e -> deleteProduct());
        productPanel.getBtnClear().addActionListener(e -> productPanel.clearForm());
        productPanel.getBtnSearch().addActionListener(e -> searchProducts());
        productPanel.getBtnRefresh().addActionListener(e -> loadProducts());
        productPanel.getBtnManageCategory().addActionListener(e -> openCategoryManager());
        
        productPanel.getProductTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = productPanel.getProductTable().getSelectedRow();
                if (row != -1) {
                    String id = productPanel.getTableModel().getValueAt(row, 0).toString();
                    Product product = productDAO.findById(id);
                    if (product != null) {
                        productPanel.fillForm(product);
                    }
                }
            }
        });
        
        productPanel.getTxtSearch().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchProducts();
                }
            }
        });
    }

    private void loadCategories() {
        var categories = categoryDAO.getAllCategories();
        productPanel.loadCategories(categories);
    }

    private void loadProducts() {
        productPanel.displayProducts(productDAO.getAllProducts());
    }

    private void addProduct() {
        try {
            Product product = getProductFromForm();
            
            if (product.getId().isEmpty() || product.getName().isEmpty() || 
                product.getCategory().isEmpty()) {
                showMessage("Vui lòng điền đầy đủ thông tin!", "Cảnh báo", 
                           JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (productDAO.insertProduct(product)) {
                showMessage("Thêm sản phẩm thành công!", "Thông báo", 
                           JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
                productPanel.clearForm();
            } else {
                showMessage("Mã sản phẩm đã tồn tại hoặc lỗi database!", "Lỗi", 
                           JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showMessage("Giá và số lượng phải là số hợp lệ!", "Lỗi", 
                       JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduct() {
        try {
            String id = productPanel.getTxtId().getText().trim();
            if (id.isEmpty()) {
                showMessage("Vui lòng chọn sản phẩm cần sửa!", "Cảnh báo", 
                           JOptionPane.WARNING_MESSAGE);
                return;
            }

            Product product = getProductFromForm();
            
            if (productDAO.updateProduct(product)) {
                showMessage("Cập nhật sản phẩm thành công!", "Thông báo", 
                           JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
                productPanel.clearForm();
            } else {
                showMessage("Không tìm thấy sản phẩm!", "Lỗi", 
                           JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showMessage("Giá và số lượng phải là số hợp lệ!", "Lỗi", 
                       JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        int row = productPanel.getProductTable().getSelectedRow();
        if (row == -1) {
            showMessage("Vui lòng chọn sản phẩm cần xóa!", "Cảnh báo", 
                       JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(productPanel, 
            "Bạn có chắc muốn xóa sản phẩm này?", "Xác nhận", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String id = productPanel.getTableModel().getValueAt(row, 0).toString();
            if (productDAO.deleteProduct(id)) {
                showMessage("Xóa sản phẩm thành công!", "Thông báo", 
                           JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
                productPanel.clearForm();
            } else {
                showMessage("Lỗi khi xóa sản phẩm!", "Lỗi", 
                           JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchProducts() {
        String keyword = productPanel.getTxtSearch().getText().trim();
        var results = productDAO.searchProducts(keyword);
        productPanel.displayProducts(results);
        
        if (results.isEmpty()) {
            showMessage("Không tìm thấy sản phẩm!", "Thông báo", 
                       JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openCategoryManager() {
        CategoryDialog dialog = new CategoryDialog((JFrame) SwingUtilities.getWindowAncestor(productPanel));
        dialog.setVisible(true);
        // Reload lại danh mục sau khi đóng dialog
        loadCategories();
    }

    private Product getProductFromForm() {
        String id = productPanel.getTxtId().getText().trim();
        String name = productPanel.getTxtName().getText().trim();
        
        Category selectedCategory = (Category) productPanel.getCboCategory().getSelectedItem();
        String category = selectedCategory != null ? selectedCategory.getName() : "";
        
        double price = Double.parseDouble(productPanel.getTxtPrice().getText().trim());
        int quantity = Integer.parseInt(productPanel.getTxtQuantity().getText().trim());
        
        return new Product(id, name, category, price, quantity);
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(productPanel, message, title, messageType);
    }
}