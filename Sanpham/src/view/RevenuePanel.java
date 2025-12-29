package view;

import dao.InvoiceDAO;
import model.Invoice;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class RevenuePanel extends JPanel {
    private InvoiceDAO invoiceDAO;
    private JTable invoiceTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTimeRange;
    private JLabel lblTotalRevenue, lblInvoiceCount, lblAverageRevenue;
    private com.toedter.calendar.JDateChooser dateFrom, dateTo;
    private JButton btnFilter, btnRefresh, btnViewDetails;

    public RevenuePanel() {
        this.invoiceDAO = new InvoiceDAO();
        initComponents();
        loadTodayRevenue();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel - Filter
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc"));

        topPanel.add(new JLabel("Khoảng thời gian:"));
        cboTimeRange = new JComboBox<>(new String[]{
            "Hôm nay", "Tuần này", "Tháng này", "Tùy chỉnh"
        });
        topPanel.add(cboTimeRange);

        topPanel.add(new JLabel("Từ:"));
        dateFrom = new com.toedter.calendar.JDateChooser();
        dateFrom.setPreferredSize(new Dimension(120, 25));
        dateFrom.setEnabled(false);
        topPanel.add(dateFrom);

        topPanel.add(new JLabel("Đến:"));
        dateTo = new com.toedter.calendar.JDateChooser();
        dateTo.setPreferredSize(new Dimension(120, 25));
        dateTo.setEnabled(false);
        topPanel.add(dateTo);

        btnFilter = createButton("Lọc", new Color(52, 152, 219));
        btnRefresh = createButton("Làm mới", new Color(149, 165, 166));
        topPanel.add(btnFilter);
        topPanel.add(btnRefresh);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel - Statistics & Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        statsPanel.add(createStatCard("Tổng doanh thu", "0 VNĐ", new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Số hóa đơn", "0", new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Trung bình/HĐ", "0 VNĐ", new Color(155, 89, 182)));

        centerPanel.add(statsPanel, BorderLayout.NORTH);

        // Invoice Table
        String[] columns = {"Mã HĐ", "Ngày giờ", "Nhân viên", "Tạm tính", "Giảm giá", "Tổng tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        invoiceTable = new JTable(tableModel);
        invoiceTable.setRowHeight(25);
        invoiceTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        invoiceTable.getTableHeader().setBackground(new Color(52, 152, 219));
        invoiceTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnViewDetails = createButton("Xem chi tiết", new Color(241, 196, 15));
        buttonPanel.add(btnViewDetails);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Event Listeners
        cboTimeRange.addActionListener(e -> {
            boolean isCustom = cboTimeRange.getSelectedIndex() == 3;
            dateFrom.setEnabled(isCustom);
            dateTo.setEnabled(isCustom);
            
            if (!isCustom) {
                loadRevenueByTimeRange();
            }
        });

        btnFilter.addActionListener(e -> loadRevenueByCustomRange());
        btnRefresh.addActionListener(e -> loadTodayRevenue());
        btnViewDetails.addActionListener(e -> viewInvoiceDetails());
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblValue;
        if (title.equals("Tổng doanh thu")) {
            lblValue = lblTotalRevenue = new JLabel(value);
        } else if (title.equals("Số hóa đơn")) {
            lblValue = lblInvoiceCount = new JLabel(value);
        } else {
            lblValue = lblAverageRevenue = new JLabel(value);
        }
        
        lblValue.setFont(new Font("Arial", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(lblValue);

        return card;
    }

    private void loadTodayRevenue() {
        LocalDate today = LocalDate.now();
        loadRevenue(today, today);
    }

    private void loadRevenueByTimeRange() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (cboTimeRange.getSelectedIndex()) {
            case 0: // Hôm nay
                startDate = endDate;
                break;
            case 1: // Tuần này
                startDate = endDate.minusDays(7);
                break;
            case 2: // Tháng này
                startDate = endDate.withDayOfMonth(1);
                break;
            default:
                return;
        }

        loadRevenue(startDate, endDate);
    }

    private void loadRevenueByCustomRange() {
        if (dateFrom.getDate() == null || dateTo.getDate() == null) {
            showMessage("Vui lòng chọn khoảng thời gian!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate start = dateFrom.getDate().toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate end = dateTo.getDate().toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        if (start.isAfter(end)) {
            showMessage("Ngày bắt đầu phải trước ngày kết thúc!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadRevenue(start, end);
    }

    private void loadRevenue(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceDAO.getInvoicesByDateRange(startDate, endDate);
        
        // Update table
        tableModel.setRowCount(0);
        double totalRevenue = 0;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Invoice inv : invoices) {
            tableModel.addRow(new Object[]{
                inv.getId(),
                inv.getCreatedDate().format(formatter),
                inv.getUserName(),
                formatMoney(inv.getSubtotal()),
                formatMoney(inv.getDiscountAmount()),
                formatMoney(inv.getTotalAmount())
            });
            totalRevenue += inv.getTotalAmount();
        }

        // Update statistics
        lblTotalRevenue.setText(formatMoney(totalRevenue));
        lblInvoiceCount.setText(String.valueOf(invoices.size()));
        
        double avgRevenue = invoices.isEmpty() ? 0 : totalRevenue / invoices.size();
        lblAverageRevenue.setText(formatMoney(avgRevenue));
    }

    private void viewInvoiceDetails() {
        int row = invoiceTable.getSelectedRow();
        if (row == -1) {
            showMessage("Vui lòng chọn hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int invoiceId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        Invoice invoice = invoiceDAO.findById(invoiceId);

        if (invoice != null) {
            InvoiceDetailDialog dialog = new InvoiceDetailDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                invoice
            );
            dialog.setVisible(true);
        }
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 30));
        return btn;
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}