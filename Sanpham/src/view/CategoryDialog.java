package view;

import dao.CategoryDAO;
import model.Category;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoryDialog extends JDialog {
    private CategoryDAO categoryDAO;
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JTextField txtCategoryName;
    private JButton btnAdd, btnUpdate, btnDelete, btnClose;
    private int selectedCategoryId = -1;

    public CategoryDialog(JFrame parent) {
        super(parent, "Quản lý danh mục", true);
        this.categoryDAO = new CategoryDAO();
        initComponents();
        loadCategories();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));

        // Panel nhập liệu
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin danh mục"));
        
        inputPanel.add(new JLabel("Tên danh mục:"));
        txtCategoryName = new JTextField(20);
        inputPanel.add(txtCategoryName);

        btnAdd = createButton("Thêm", new Color(46, 204, 113));
        btnUpdate = createButton("Sửa", new Color(52, 152, 219));
        btnDelete = createButton("Xóa", new Color(231, 76, 60));
        
        inputPanel.add(btnAdd);
        inputPanel.add(btnUpdate);
        inputPanel.add(btnDelete);

        add(inputPanel, BorderLayout.NORTH);

        // Bảng danh mục
        String[] columns = {"ID", "Tên danh mục"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(25);
        categoryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        categoryTable.getTableHeader().setBackground(new Color(52, 152, 219));
        categoryTable.getTableHeader().setForeground(Color.BLACK);
        categoryTable.setSelectionBackground(new Color(174, 214, 241));

        categoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = categoryTable.getSelectedRow();
                if (row != -1) {
                    selectedCategoryId = (int) tableModel.getValueAt(row, 0);
                    String name = tableModel.getValueAt(row, 1).toString();
                    txtCategoryName.setText(name);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Nút đóng
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnClose = createButton("Đóng", new Color(149, 165, 166));
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event listeners
        btnAdd.addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnClose.addActionListener(e -> dispose());
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 30));
        return button;
    }

    private void loadCategories() {
        List<Category> categories = categoryDAO.getAllCategories();
        tableModel.setRowCount(0);
        for (Category cat : categories) {
            tableModel.addRow(new Object[]{cat.getId(), cat.getName()});
        }
    }

    private void addCategory() {
        String name = txtCategoryName.getText().trim();
        
        if (name.isEmpty()) {
            showMessage("Vui lòng nhập tên danh mục!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (categoryDAO.categoryExists(name)) {
            showMessage("Danh mục đã tồn tại!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Category category = new Category(0, name);
        if (categoryDAO.insertCategory(category)) {
            showMessage("Thêm danh mục thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadCategories();
            clearForm();
        } else {
            showMessage("Lỗi khi thêm danh mục!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCategory() {
        if (selectedCategoryId == -1) {
            showMessage("Vui lòng chọn danh mục cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtCategoryName.getText().trim();
        
        if (name.isEmpty()) {
            showMessage("Vui lòng nhập tên danh mục!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Category category = new Category(selectedCategoryId, name);
        if (categoryDAO.updateCategory(category)) {
            showMessage("Cập nhật danh mục thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadCategories();
            clearForm();
        } else {
            showMessage("Lỗi khi cập nhật danh mục!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategory() {
        if (selectedCategoryId == -1) {
            showMessage("Vui lòng chọn danh mục cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa danh mục này?\nLưu ý: Các sản phẩm thuộc danh mục này sẽ bị ảnh hưởng!",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (categoryDAO.deleteCategory(selectedCategoryId)) {
                showMessage("Xóa danh mục thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
                clearForm();
            } else {
                showMessage("Lỗi khi xóa danh mục!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtCategoryName.setText("");
        selectedCategoryId = -1;
        categoryTable.clearSelection();
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}