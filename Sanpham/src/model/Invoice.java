package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Model Hóa đơn
public class Invoice {
    private int id;
    private LocalDateTime createdDate;
    private int userId;
    private String userName;
    private double subtotal;
    private double discountAmount;
    private double totalAmount;
    private String discountCode;
    private List<InvoiceDetail> details;

    public Invoice() {
        this.details = new ArrayList<>();
    }

    public Invoice(int id, LocalDateTime createdDate, int userId, String userName, 
                   double subtotal, double discountAmount, double totalAmount, String discountCode) {
        this.id = id;
        this.createdDate = createdDate;
        this.userId = userId;
        this.userName = userName;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.discountCode = discountCode;
        this.details = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
    
    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }

    public void addDetail(InvoiceDetail detail) {
        this.details.add(detail);
    }
}
