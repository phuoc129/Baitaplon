# Baitaplon
# Web Scraper Việc Làm - danang43.edu.vn

Dự án này thu thập dữ liệu việc làm từ website [danang43.edu.vn](https://danang43.edu.vn), bao gồm thông tin như: Tiêu đề, Mô tả, Công ty, Lương, Địa chỉ làm việc. Kết quả được lưu vào file CSV và tự động chạy vào 6h sáng mỗi ngày.

---

## ✅ Tính năng

- Truy cập website danang43.edu.vn
- Lấy thông tin:
  - Tiêu đề công việc
  - Mô tả công việc
  - Công ty tuyển dụng
  - Lương
  - Địa chỉ làm việc
- Hỗ trợ đọc từ trang chi tiết và từ bảng danh sách.
- Duyệt nhiều trang.
- Lưu dữ liệu vào file `danang43.csv`.
- Chạy tự động mỗi ngày lúc 06:00 bằng thư viện `schedule`.

---

## 🧪 Yêu cầu hệ thống

- Python 3.8 trở lên
- Các thư viện: `requests`, `beautifulsoup4`, `pandas`, `schedule`

---

## ⚙️ Cài đặt

### 1. Clone project

```bash
git clone https://github.com/phuoc129/Baitaplon.git
cd Baitaplon
