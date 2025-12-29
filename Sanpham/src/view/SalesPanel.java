package view;

import dao.ProductDAO;
import dao.DiscountDAO;
import dao.InvoiceDAO;
import model.Product;
import model.User;
import model.Invoice;
import model.InvoiceDetail;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesPanel extends JPanel {
    private ProductDAO productDAO;
    private DiscountDAO discountDAO;
    private InvoiceDAO invoiceDAO;
    private User currentUser;
    
    private JTable productTable, cartTable;
    private DefaultTableModel productModel, cartModel;
    private JTextField txtSearchProduct, txtDiscountCode;
    private JLabel lblSubtotal, lblDiscount, lblTotal;
    private JButton btnAddToCart, btnRemoveFromCart, btnClearCart, btnCheckout, btnSearch;
    
    private Map<String, CartItem> cart;
    private double subtotal = 0;
    private double discountAmount = 0;
    private double totalAmount = 0;

    public SalesPanel(User currentUser) {
        this.currentUser = currentUser;
        this.productDAO = new ProductDAO();
        this.discountDAO = new DiscountDAO();
        this.invoiceDAO = new InvoiceDAO();
        this.cart = new HashMap<>();
        
        initComponents();
        loadProducts();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel - Product Selection
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm sản phẩm:"));
        txtSearchProduct = new JTextField(25);
        searchPanel.add(txtSearchProduct);
        btnSearch = createButton("Tìm", new Color(52, 152, 219), 80, 30);
        searchPanel.add(btnSearch);
        topPanel.add(searchPanel, BorderLayout.NORTH);

        // Product Table
        String[] productColumns = {"Mã SP", "Tên sản phẩm", "Danh mục", "Giá", "Tồn kho"};
        productModel = new DefaultTableModel(productColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productModel);
        productTable.setRowHeight(25);
        styleTable(productTable);
        
        JScrollPane productScroll = new JScrollPane(productTable);
        productScroll.setPreferredSize(new Dimension(0, 200));
        topPanel.add(productScroll, BorderLayout.CENTER);

        JPanel productButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAddToCart = createButton("Thêm vào giỏ →", new Color(46, 204, 113), 150, 35);
        productButtonPanel.add(btnAddToCart);
        topPanel.add(productButtonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel - Cart
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
            "Giỏ hàng",
            0, 0, new Font("Arial", Font.BOLD, 14), new Color(231, 76, 60)
        ));

        String[] cartColumns = {"Mã SP", "Tên sản phẩm", "Giá", "SL", "Thành tiền"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Chỉ cột số lượng có thể edit
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(25);
        styleTable(cartTable);
        
        JScrollPane cartScroll = new JScrollPane(cartTable);
        centerPanel.add(cartScroll, BorderLayout.CENTER);

        JPanel cartButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRemoveFromCart = createButton("Xóa", new Color(231, 76, 60), 100, 30);
        btnClearCart = createButton("Xóa tất cả", new Color(149, 165, 166), 120, 30);
        cartButtonPanel.add(btnRemoveFromCart);
        cartButtonPanel.add(btnClearCart);
        centerPanel.add(cartButtonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Right Panel - Checkout
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel summaryPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        
        summaryPanel.add(createLabel("Tạm tính:", Font.PLAIN));
        lblSubtotal = createLabel("0 VNĐ", Font.BOLD);
        summaryPanel.add(lblSubtotal);

        summaryPanel.add(createLabel("Mã giảm giá:", Font.PLAIN));
        txtDiscountCode = new JTextField();
        txtDiscountCode.setFont(new Font("Arial", Font.PLAIN, 13));
        summaryPanel.add(txtDiscountCode);

        summaryPanel.add(createLabel("Giảm giá:", Font.PLAIN));
        lblDiscount = createLabel("0 VNĐ", Font.BOLD);
        lblDiscount.setForeground(new Color(231, 76, 60));
        summaryPanel.add(lblDiscount);

        summaryPanel.add(new JLabel("")); // Spacer
        summaryPanel.add(new JLabel(""));

        summaryPanel.add(createLabel("TỔNG TIỀN:", Font.BOLD, 16));
        lblTotal = createLabel("0 VNĐ", Font.BOLD, 18);
        lblTotal.setForeground(new Color(46, 204, 113));
        summaryPanel.add(lblTotal);

        rightPanel.add(summaryPanel, BorderLayout.CENTER);

        btnCheckout = createButton("THANH TOÁN", new Color(46, 204, 113), 0, 50);
        btnCheckout.setFont(new Font("Arial", Font.BOLD, 16));
        rightPanel.add(btnCheckout, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);

        // Event Listeners
        btnSearch.addActionListener(e -> searchProducts());
        btnAddToCart.addActionListener(e -> addToCart());
        btnRemoveFromCart.addActionListener(e -> removeFromCart());
        btnClearCart.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> checkout());
        
        txtDiscountCode.addActionListener(e -> applyDiscount());
        
        // Double click to add product
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    addToCart();
                }
            }
        });

        // Update quantity in cart
        cartModel.addTableModelListener(e -> {
            if (e.getColumn() == 3) { // Cột số lượng
                updateCartItemQuantity(e.getFirstRow());
            }
        });
    }

    private void loadProducts() {
        List<Product> products = productDAO.getAllProducts();
        displayProducts(products);
    }

    private void displayProducts(List<Product> products) {
        productModel.setRowCount(0);
        for (Product p : products) {
            productModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategory(),
                formatMoney(p.getPrice()), p.getQuantity()
            });
        }
    }

    private void searchProducts() {
        String keyword = txtSearchProduct.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
        } else {
            displayProducts(productDAO.searchProducts(keyword));
        }
    }

    private void addToCart() {
        int row = productTable.getSelectedRow();
        if (row == -1) {
            showMessage("Vui lòng chọn sản phẩm!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = productModel.getValueAt(row, 0).toString();
        Product product = productDAO.findById(productId);

        if (product == null) {
            showMessage("Không tìm thấy sản phẩm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (product.getQuantity() <= 0) {
            showMessage("Sản phẩm đã hết hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CartItem item = cart.get(productId);
        if (item == null) {
            cart.put(productId, new CartItem(product, 1));
        } else {
            if (item.quantity < product.getQuantity()) {
                item.quantity++;
            } else {
                showMessage("Không đủ hàng trong kho!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        updateCartDisplay();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row == -1) {
            showMessage("Vui lòng chọn sản phẩm cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = cartModel.getValueAt(row, 0).toString();
        cart.remove(productId);
        updateCartDisplay();
    }

    private void clearCart() {
        if (cart.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa tất cả sản phẩm trong giỏ?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            cart.clear();
            updateCartDisplay();
        }
    }

    private void updateCartItemQuantity(int row) {
        try {
            String productId = cartModel.getValueAt(row, 0).toString();
            int newQuantity = Integer.parseInt(cartModel.getValueAt(row, 3).toString());

            CartItem item = cart.get(productId);
            if (item != null) {
                if (newQuantity <= 0) {
                    cart.remove(productId);
                } else if (newQuantity <= item.product.getQuantity()) {
                    item.quantity = newQuantity;
                } else {
                    showMessage("Không đủ hàng trong kho!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    cartModel.setValueAt(item.quantity, row, 3);
                    return;
                }
                updateCartDisplay();
            }
        } catch (NumberFormatException e) {
            showMessage("Số lượng phải là số nguyên dương!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            updateCartDisplay();
        }
    }

    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        subtotal = 0;

        for (CartItem item : cart.values()) {
            double itemTotal = item.product.getPrice() * item.quantity;
            subtotal += itemTotal;
            cartModel.addRow(new Object[]{
                item.product.getId(),
                item.product.getName(),
                formatMoney(item.product.getPrice()),
                item.quantity,
                formatMoney(itemTotal)
            });
        }

        applyDiscount();
    }

    private void applyDiscount() {
        String code = txtDiscountCode.getText().trim();
        
        if (code.isEmpty()) {
            discountAmount = 0;
        } else {
            discountAmount = discountDAO.calculateDiscount(code, subtotal);
            if (discountAmount == 0) {
                txtDiscountCode.setBackground(new Color(255, 200, 200));
            } else {
                txtDiscountCode.setBackground(new Color(200, 255, 200));
            }
        }

        totalAmount = subtotal - discountAmount;

        lblSubtotal.setText(formatMoney(subtotal));
        lblDiscount.setText(formatMoney(discountAmount));
        lblTotal.setText(formatMoney(totalAmount));
    }

    private void checkout() {
        if (cart.isEmpty()) {
            showMessage("Giỏ hàng trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận thanh toán " + formatMoney(totalAmount) + "?",
            "Xác nhận thanh toán",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Tạo hóa đơn
        Invoice invoice = new Invoice();
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setUserId(currentUser.getId());
        invoice.setUserName(currentUser.getFullName());
        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setDiscountCode(txtDiscountCode.getText().trim());

        // Thêm chi tiết
        for (CartItem item : cart.values()) {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setProductId(item.product.getId());
            detail.setProductName(item.product.getName());
            detail.setPrice(item.product.getPrice());
            detail.setQuantity(item.quantity);
            detail.setSubtotal(item.product.getPrice() * item.quantity);
            invoice.addDetail(detail);
        }

        // Lưu vào database
        if (invoiceDAO.saveInvoice(invoice)) {
            showMessage(
                "Thanh toán thành công!\n" +
                "Tổng tiền: " + formatMoney(totalAmount) +
                "\nCảm ơn quý khách!",
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            cart.clear();
            txtDiscountCode.setText("");
            updateCartDisplay();
            loadProducts();
        } else {
            showMessage("Lỗi khi lưu hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper methods
    private JButton createButton(String text, Color color, int width, int height) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (width > 0) btn.setPreferredSize(new Dimension(width, height));
        return btn;
    }

    private JLabel createLabel(String text, int style) {
        return createLabel(text, style, 13);
    }

    private JLabel createLabel(String text, int style, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", style, fontSize));
        return label;
    }

    private void styleTable(JTable table) {
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(174, 214, 241));
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Inner class for cart items
    private class CartItem {
        Product product;
        int quantity;

        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}