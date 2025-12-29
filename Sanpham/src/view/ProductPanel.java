package view;

import model.Product;
import model.Category;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtId, txtName, txtPrice, txtQuantity, txtSearch;
    private JComboBox<Category> cboCategory;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch, btnRefresh, btnManageCategory;
    
    public ProductPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(350, 0));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Thông tin sản phẩm",
            0, 0, new Font("Arial", Font.BOLD, 14), new Color(52, 152, 219)
        ));

        formPanel.add(new JLabel("Mã sản phẩm:"));
        txtId = new JTextField();
        formPanel.add(txtId);

        formPanel.add(new JLabel("Tên sản phẩm:"));
        txtName = new JTextField();
        formPanel.add(txtName);

        formPanel.add(new JLabel("Danh mục:"));
        cboCategory = new JComboBox<>();
        cboCategory.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(cboCategory);

        formPanel.add(new JLabel("Giá bán (VNĐ):"));
        txtPrice = new JTextField();
        formPanel.add(txtPrice);

        formPanel.add(new JLabel("Số lượng:"));
        txtQuantity = new JTextField();
        formPanel.add(txtQuantity);

        // Nút quản lý danh mục
        formPanel.add(new JLabel(""));
        btnManageCategory = createSmallButton("Quản lý danh mục", new Color(155, 89, 182));
        formPanel.add(btnManageCategory);

        leftPanel.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnAdd = createButton("Thêm", new Color(46, 204, 113));
        btnUpdate = createButton("Sửa", new Color(52, 152, 219));
        btnDelete = createButton("Xóa", new Color(231, 76, 60));
        btnClear = createButton("Làm mới", new Color(149, 165, 166));

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createTablePanel() {
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Tìm kiếm:"));
        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);
        btnSearch = createButton("Tìm", new Color(241, 196, 15));
        btnSearch.setPreferredSize(new Dimension(80, 30));
        searchPanel.add(btnSearch);
        
        btnRefresh = createButton("Làm mới", new Color(52, 152, 219));
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        searchPanel.add(btnRefresh);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Mã SP", "Tên sản phẩm", "Danh mục", "Giá bán", "Số lượng"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        productTable.getTableHeader().setBackground(new Color(52, 152, 219));
        productTable.getTableHeader().setForeground(Color.BLACK);
        productTable.setSelectionBackground(new Color(174, 214, 241));

        JScrollPane scrollPane = new JScrollPane(productTable);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        return rightPanel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setFocusPainted(false);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSmallButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Getter methods
    public JTable getProductTable() { return productTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtId() { return txtId; }
    public JTextField getTxtName() { return txtName; }
    public JComboBox<Category> getCboCategory() { return cboCategory; }
    public JTextField getTxtPrice() { return txtPrice; }
    public JTextField getTxtQuantity() { return txtQuantity; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnClear() { return btnClear; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnManageCategory() { return btnManageCategory; }

    public void displayProducts(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(p.toTableRow());
        }
    }

    public void loadCategories(List<Category> categories) {
        cboCategory.removeAllItems();
        for (Category cat : categories) {
            cboCategory.addItem(cat);
        }
    }

    public void clearForm() {
        txtId.setText("");
        txtName.setText("");
        if (cboCategory.getItemCount() > 0) {
            cboCategory.setSelectedIndex(0);
        }
        txtPrice.setText("");
        txtQuantity.setText("");
        txtId.setEditable(true);
        productTable.clearSelection();
    }

    public void fillForm(Product product) {
        txtId.setText(product.getId());
        txtName.setText(product.getName());
        
        // Tìm và chọn category trong ComboBox
        for (int i = 0; i < cboCategory.getItemCount(); i++) {
            if (cboCategory.getItemAt(i).getName().equals(product.getCategory())) {
                cboCategory.setSelectedIndex(i);
                break;
            }
        }
        
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtQuantity.setText(String.valueOf(product.getQuantity()));
        txtId.setEditable(false);
    }
}